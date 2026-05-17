package com.example.homestaymanager.service.impl;

import com.example.homestaymanager.dto.request.CreateBookingRequest;
import com.example.homestaymanager.dto.response.BookingResponse;
import com.example.homestaymanager.enums.BookingStatus;
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
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private static final List<BookingStatus> BLOCKING_STATUSES = List.of(
            BookingStatus.PENDING,
            BookingStatus.CONFIRMED,
            BookingStatus.CHECKED_IN);

    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final RoomRepository roomRepository;
    private final RoomPricingRepository roomPricingRepository;

    @Override
    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {
        Objects.requireNonNull(request, "request");
        validateTimes(request.getCheckIn(), request.getCheckOut());
        if (request.getGuestCount() < 1) {
            throw new RuntimeException("Số lượng khách phải >= 1");
        }

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại"));

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
        booking.setTotalAmount(total);
        booking.setPaidAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        booking.setHasSentReminder(false);

        bookingRepository.save(booking);
        return toResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(int id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        return toResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getBookings(Integer customerId, Integer roomId, Integer branchId, BookingStatus status, int page, int size) {
        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = 20;
        }
        Pageable pageable = PageRequest.of(page, size);
        return bookingRepository.findByFilters(customerId, roomId, branchId, status, pageable)
                .map(BookingServiceImpl::toResponse);
    }

    @Override
    @Transactional
    public BookingResponse updateStatus(int bookingId, BookingStatus newStatus) {
        if (newStatus == null) {
            throw new RuntimeException("status là bắt buộc");
        }
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        assertStatusTransition(booking.getCurrentStatus(), newStatus);
        booking.setCurrentStatus(newStatus);
        return toResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(int id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        BookingStatus s = booking.getCurrentStatus();
        if (s != BookingStatus.PENDING && s != BookingStatus.CONFIRMED) {
            throw new RuntimeException("Chỉ có thể hủy booking đang PENDING hoặc CONFIRMED");
        }
        if (!booking.getCheckIn().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Không thể hủy booking đã bắt đầu hoặc đã quá hạn");
        }
        booking.setCurrentStatus(BookingStatus.CANCELLED);
        return toResponse(booking);
    }

    private void validateTimes(LocalDateTime checkIn, LocalDateTime checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new RuntimeException("Ngày checkIn và checkOut là bắt buộc");
        }
        if (!checkIn.isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Ngày checkIn phải sau thời điểm hiện tại");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new RuntimeException("Ngày checkOut phải sau Ngày checkIn");
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
        DayOfWeek dow = night.getDayOfWeek();
        boolean weekend = dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
        if (weekend && pricing.getWeekendPrice() != null) {
            return pricing.getWeekendPrice();
        }
        return base;
    }

    private static boolean isHoliday(LocalDate date) {
        Set<MonthDay> weekendHolidays = Set.of(
                MonthDay.of(1, 1),
                MonthDay.of(4, 30),
                MonthDay.of(5, 1),
                MonthDay.of(9, 2)
        );
        return weekendHolidays.contains(MonthDay.from(date));
    }

    private static int computeRefundPercentage(LocalDateTime checkIn, LocalDateTime cancellationTime) {
        if (cancellationTime.isBefore(checkIn.minusHours(24))) {
            return 100;
        }
        if (cancellationTime.isBefore(checkIn)) {
            return 50;
        }
        return 0;
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
                if (to != BookingStatus.CHECKED_IN && to != BookingStatus.CANCELLED && to != BookingStatus.NO_SHOW) {
                    throw new RuntimeException("Không thể chuyển CONFIRMED sang " + to);
                }
            }
            case CHECKED_IN -> {
                if (to != BookingStatus.CHECKED_OUT) {
                    throw new RuntimeException("Chỉ có thể chuyển CHECKED_IN sang CHECKED_OUT");
                }
            }
            default -> throw new RuntimeException("Không thể đổi trạng thái từ " + from);
        }
    }

    private static BookingResponse toResponse(Booking b) {
        Integer employeeId = b.getEmployee() != null ? b.getEmployee().getId() : null;
        Integer branchId = b.getRoom().getBranch() != null ? b.getRoom().getBranch().getId() : null;
        Integer refundPercentage = null;
        if (b.getCurrentStatus() == BookingStatus.CANCELLED) {
            LocalDateTime referenceTime = b.getUpdatedAt() != null ? b.getUpdatedAt() : LocalDateTime.now();
            refundPercentage = computeRefundPercentage(b.getCheckIn(), referenceTime);
        }
        return BookingResponse.builder()
                .id(b.getId())
                .customerId(b.getCustomer().getId())
                .customerName(b.getCustomer().getName())
                .employeeId(employeeId)
                .roomId(b.getRoom().getId())
                .branchId(branchId)
                .roomTypeName(b.getRoom().getRoomType().getName())
                .checkIn(b.getCheckIn())
                .checkOut(b.getCheckOut())
                .guestCount(b.getGuestCount())
                .currentStatus(b.getCurrentStatus())
                .totalAmount(b.getTotalAmount())
                .paidAmount(b.getPaidAmount())
                .hasSentReminder(b.isHasSentReminder())
                .refundPercentage(refundPercentage)
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }
}
