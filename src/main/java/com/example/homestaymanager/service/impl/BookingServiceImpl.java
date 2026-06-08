package com.example.homestaymanager.service.impl;

import com.example.homestaymanager.dto.request.CreateBookingRequest;
import com.example.homestaymanager.dto.response.BookingCalendarResponse;
import com.example.homestaymanager.dto.response.BookingResponse;
import com.example.homestaymanager.enums.BookingStatus;
import com.example.homestaymanager.enums.RoomStatus;
import com.example.homestaymanager.model.Booking;
import com.example.homestaymanager.model.Customer;
import com.example.homestaymanager.model.Employee;
import com.example.homestaymanager.model.Room;
import com.example.homestaymanager.model.RoomPricing;
import com.example.homestaymanager.repository.BookingRepository;
import com.example.homestaymanager.repository.CustomerRepository;
import com.example.homestaymanager.repository.EmployeeRepository;
import com.example.homestaymanager.repository.RoomPricingRepository;
import com.example.homestaymanager.repository.RoomRepository;
import com.example.homestaymanager.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.MonthDay;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private static final List<BookingStatus> BLOCKING_STATUSES = List.of(
            BookingStatus.PENDING,
            BookingStatus.CONFIRMED);

    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final RoomRepository roomRepository;
    private final RoomPricingRepository roomPricingRepository;

    @Value("${app.booking.pending-hold-minutes:30}")
    private int pendingHoldMinutes;

    @Override
    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {
        Objects.requireNonNull(request, "request");
        validateTimes(request.getCheckIn(), request.getCheckOut());
        if (request.getGuestCount() < 1) {
            throw new RuntimeException("Số lượng khách phải >= 1");
        }

        Customer customer = resolveCustomer(request);
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại"));
        if (Boolean.FALSE.equals(room.getActive())) {
            throw new RuntimeException("Phòng đã ngừng kinh doanh, không thể đặt phòng");
        }

        int maxGuest = room.getRoomType().getMaxGuest();
        if (request.getGuestCount() > maxGuest) {
            throw new RuntimeException("Số lượng khách vượt quá sức chứa loại phòng (" + maxGuest + ")");
        }

        Employee employee = null;
        if (request.getEmployeeId() != null) {
            employee = employeeRepository.findById(request.getEmployeeId())
                    .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại"));
        }

        if (bookingRepository.countOverlapping(
                room.getId(),
                request.getCheckIn(),
                request.getCheckOut(),
                BLOCKING_STATUSES) > 0) {
            throw new RuntimeException("Phòng đã được đặt trong khoảng thời gian này");
        }

        BigDecimal total = computeTotal(room, request.getCheckIn(), request.getCheckOut());

        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setEmployee(employee);
        booking.setRoom(room);
        booking.setCheckIn(request.getCheckIn());
        booking.setCheckOut(request.getCheckOut());
        booking.setGuestCount(request.getGuestCount());
        booking.setCurrentStatus(BookingStatus.PENDING);
        booking.setPendingExpiresAt(LocalDateTime.now().plusMinutes(resolvePendingHoldMinutes()));
        booking.setTotalAmount(total);
        booking.setPaidAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        booking.setHasSentReminder(false);

        bookingRepository.save(booking);
        return toResponse(booking);
    }

    private int resolvePendingHoldMinutes() {
        return pendingHoldMinutes > 0 ? pendingHoldMinutes : 30;
    }

    private Customer resolveCustomer(CreateBookingRequest request) {
        if (request.getCustomerId() > 0) {
            return customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));
        }

        String keyword = request.getCustomerKeyword();
        if (keyword == null || keyword.isBlank()) {
            throw new RuntimeException("Vui lòng nhập tên hoặc email khách hàng");
        }

        String normalized = keyword.trim();
        if (normalized.contains("@")) {
            return customerRepository.findByEmailIgnoreCase(normalized)
                    .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));
        }

        List<Customer> exactMatches = customerRepository.findByNameIgnoreCase(normalized);
        if (exactMatches.size() == 1) {
            return exactMatches.get(0);
        }
        if (exactMatches.size() > 1) {
            throw new RuntimeException("Có nhiều khách hàng trùng tên, vui lòng nhập email");
        }

        String lowerKeyword = normalized.toLowerCase(Locale.ROOT);
        List<Customer> matches = customerRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneContainingIgnoreCase(
                normalized,
                normalized,
                normalized);
        List<Customer> filtered = matches.stream()
                .filter(customer -> customer.getName() != null && customer.getName().toLowerCase(Locale.ROOT).contains(lowerKeyword))
                .toList();
        if (filtered.size() == 1) {
            return filtered.get(0);
        }
        if (filtered.size() > 1) {
            throw new RuntimeException("Có nhiều khách hàng phù hợp, vui lòng nhập email");
        }

        throw new RuntimeException("Khách hàng không tồn tại");
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(int id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại"));
        return toResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getBookings(Integer customerId, String customerName, Integer roomId, Integer branchId, BookingStatus status, LocalDate dateFrom, LocalDate dateTo, int page, int size) {
        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = 20;
        }
        Pageable pageable = PageRequest.of(page, size);
        String normalizedCustomerName = customerName != null && !customerName.isBlank()
                ? customerName.trim()
                : null;
        LocalDateTime from = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime to = dateTo != null ? dateTo.plusDays(1).atStartOfDay() : null;
        return bookingRepository.findByFilters(customerId, normalizedCustomerName, roomId, branchId, status, from, to, pageable)
                .map(BookingServiceImpl::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingCalendarResponse> getRoomBookingCalendar(int roomId, LocalDate dateFrom, LocalDate dateTo) {
        roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại"));
        LocalDate fromDate = dateFrom != null ? dateFrom : LocalDate.now();
        LocalDate toDate = dateTo != null ? dateTo : fromDate.plusMonths(6);
        if (!toDate.isAfter(fromDate)) {
            throw new RuntimeException("Khoảng thời gian xem lịch không hợp lệ");
        }

        return bookingRepository.findBlockingBookingsForCalendar(
                        roomId,
                        fromDate.atStartOfDay(),
                        toDate.plusDays(1).atStartOfDay(),
                        BLOCKING_STATUSES)
                .stream()
                .map(booking -> BookingCalendarResponse.builder()
                        .checkIn(booking.getCheckIn())
                        .checkOut(booking.getCheckOut())
                        .status(booking.getCurrentStatus())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public BookingResponse updateStatus(int bookingId, BookingStatus newStatus) {
        if (newStatus == null) {
            throw new RuntimeException("Trạng thái là bắt buộc");
        }
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại"));
        assertStatusTransition(booking.getCurrentStatus(), newStatus);
        booking.setCurrentStatus(newStatus);
        if (newStatus == BookingStatus.CONFIRMED) {
            booking.setPaidAmount(booking.getTotalAmount());
            refreshRoomStatusFromCurrentBookings(booking.getRoom());
        } else if (newStatus == BookingStatus.CANCELLED || newStatus == BookingStatus.NO_SHOW) {
            refreshRoomStatusFromCurrentBookings(booking.getRoom());
        }
        return toResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(int id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại"));
        BookingStatus status = booking.getCurrentStatus();
        if (status != BookingStatus.PENDING && status != BookingStatus.CONFIRMED) {
            throw new RuntimeException("Chỉ có thể hủy booking đang PENDING hoặc CONFIRMED");
        }
        if (!booking.getCheckIn().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Không thể hủy booking đã bắt đầu hoặc đã quá hạn");
        }
        booking.setCurrentStatus(BookingStatus.CANCELLED);
        refreshRoomStatusFromCurrentBookings(booking.getRoom());
        return toResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse checkIn(int bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại"));
        if (booking.getCurrentStatus() != BookingStatus.CONFIRMED) {
            throw new RuntimeException("Chỉ booking đã xác nhận mới có thể check-in");
        }
        if (booking.getActualCheckOutAt() != null) {
            throw new RuntimeException("Booking đã check-out");
        }
        if (booking.getActualCheckInAt() == null) {
            booking.setActualCheckInAt(LocalDateTime.now());
        }
        booking.getRoom().setStatus(RoomStatus.OCCUPIED);
        booking.getRoom().setCleaningStartedAt(null);
        return toResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse checkOut(int bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại"));
        if (booking.getCurrentStatus() != BookingStatus.CONFIRMED) {
            throw new RuntimeException("Chỉ booking đã xác nhận mới có thể check-out");
        }
        if (booking.getActualCheckInAt() == null) {
            throw new RuntimeException("Booking chưa check-in");
        }
        if (booking.getActualCheckOutAt() == null) {
            booking.setActualCheckOutAt(LocalDateTime.now());
        }
        booking.getRoom().setStatus(RoomStatus.CLEANING);
        booking.getRoom().setCleaningStartedAt(LocalDateTime.now());
        return toResponse(booking);
    }

    private void refreshRoomStatusFromCurrentBookings(Room room) {
        RoomStatus nextStatus = resolveCurrentRoomStatus(room.getId());
        room.setStatus(nextStatus);
        if (nextStatus != RoomStatus.CLEANING) {
            room.setCleaningStartedAt(null);
        }
    }

    private RoomStatus resolveCurrentRoomStatus(int roomId) {
        LocalDateTime now = LocalDateTime.now();
        if (bookingRepository
                .findFirstByRoomIdAndCurrentStatusAndActualCheckInAtIsNotNullAndActualCheckOutAtIsNullOrderByActualCheckInAtDesc(
                        roomId,
                        BookingStatus.CONFIRMED)
                .isPresent()) {
            return RoomStatus.OCCUPIED;
        }
        if (bookingRepository
                .findFirstByRoomIdAndCurrentStatusAndActualCheckInAtIsNullAndActualCheckOutAtIsNullAndCheckInLessThanEqualAndCheckOutAfterOrderByCheckInAsc(
                        roomId,
                        BookingStatus.CONFIRMED,
                        now,
                        now)
                .isPresent()) {
            return RoomStatus.WAITING_CHECKIN;
        }
        return RoomStatus.AVAILABLE;
    }

    private void validateTimes(LocalDateTime checkIn, LocalDateTime checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new RuntimeException("Ngày check-in và check-out là bắt buộc");
        }
        if (!checkIn.isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Ngày check-in phải sau thời điểm hiện tại");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new RuntimeException("Ngày check-out phải sau ngày check-in");
        }
        if (!checkOut.toLocalDate().isAfter(checkIn.toLocalDate())) {
            throw new RuntimeException("Phải đặt tối thiểu 1 đêm");
        }
    }

    private BigDecimal computeTotal(Room room, LocalDateTime checkIn, LocalDateTime checkOut) {
        int roomTypeId = room.getRoomType().getId();
        List<RoomPricing> applicable = roomPricingRepository.findApplicableForStay(
                roomTypeId, checkIn, checkOut);
        if (applicable.isEmpty()) {
            throw new RuntimeException("Chưa có bảng giá áp dụng cho loại phòng trong khoảng thời gian này");
        }
        RoomPricing pricing = applicable.stream()
                .max(Comparator.comparing(RoomPricing::getStartDate))
                .orElseThrow();

        LocalDate firstNight = checkIn.toLocalDate();
        long nights = ChronoUnit.DAYS.between(checkIn.toLocalDate(), checkOut.toLocalDate());
        if (nights < 1) {
            nights = 1;
        }

        BigDecimal total = BigDecimal.ZERO;
        for (long i = 0; i < nights; i++) {
            LocalDate night = firstNight.plusDays(i);
            BigDecimal nightRate = rateForNight(pricing, night);
            total = total.add(nightRate);
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal rateForNight(RoomPricing pricing, LocalDate night) {
        BigDecimal base = pricing.getBasePrice() != null
                ? pricing.getBasePrice()
                : BigDecimal.ZERO;
        if (pricing.getHolidayPrice() != null && isHoliday(night)) {
            return pricing.getHolidayPrice();
        }
        DayOfWeek dayOfWeek = night.getDayOfWeek();
        boolean weekend = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
        if (weekend && pricing.getWeekendPrice() != null) {
            return pricing.getWeekendPrice();
        }
        return base;
    }

    private static boolean isHoliday(LocalDate date) {
        Set<MonthDay> holidays = Set.of(
                MonthDay.of(1, 1),
                MonthDay.of(4, 30),
                MonthDay.of(5, 1),
                MonthDay.of(9, 2)
        );
        return holidays.contains(MonthDay.from(date));
    }

    private static void assertStatusTransition(BookingStatus from, BookingStatus to) {
        if (from == to) {
            return;
        }
        switch (from) {
            case PENDING -> {
                if (to != BookingStatus.CONFIRMED && to != BookingStatus.CANCELLED && to != BookingStatus.NO_SHOW) {
                    throw new RuntimeException("Không thể chuyển PENDING sang " + to);
                }
            }
            case CONFIRMED -> {
                if (to != BookingStatus.CANCELLED && to != BookingStatus.NO_SHOW) {
                    throw new RuntimeException("Không thể chuyển CONFIRMED sang " + to);
                }
            }
            default -> throw new RuntimeException("Không thể đổi trạng thái từ " + from);
        }
    }

    public static BookingResponse toResponse(Booking booking) {
        Integer employeeId = booking.getEmployee() != null ? booking.getEmployee().getId() : null;
        String employeeName = booking.getEmployee() != null ? booking.getEmployee().getName() : null;
        Integer branchId = booking.getRoom().getBranch() != null ? booking.getRoom().getBranch().getId() : null;
        return BookingResponse.builder()
                .id(booking.getId())
                .customerId(booking.getCustomer().getId())
                .customerName(booking.getCustomer().getName())
                .employeeId(employeeId)
                .employeeName(employeeName)
                .roomId(booking.getRoom().getId())
                .roomName(resolveRoomName(booking.getRoom()))
                .branchId(branchId)
                .roomTypeName(booking.getRoom().getRoomType().getName())
                .checkIn(booking.getCheckIn())
                .checkOut(booking.getCheckOut())
                .guestCount(booking.getGuestCount())
                .currentStatus(booking.getCurrentStatus())
                .actualCheckInAt(booking.getActualCheckInAt())
                .actualCheckOutAt(booking.getActualCheckOutAt())
                .totalAmount(booking.getTotalAmount())
                .paidAmount(booking.getPaidAmount())
                .hasSentReminder(booking.isHasSentReminder())
                .pendingExpiresAt(booking.getPendingExpiresAt())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }

    private static String resolveRoomName(Room room) {
        if (room.getName() != null && !room.getName().isBlank()) {
            return room.getName().trim();
        }
        return "Phòng";
    }
}
