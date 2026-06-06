package com.example.homestaymanager.service.impl;

import com.example.homestaymanager.dto.response.StatisticsResponse;
import com.example.homestaymanager.enums.BookingStatus;
import com.example.homestaymanager.enums.CustomerStatus;
import com.example.homestaymanager.enums.RoomStatus;
import com.example.homestaymanager.model.Booking;
import com.example.homestaymanager.model.Room;
import com.example.homestaymanager.repository.BookingRepository;
import com.example.homestaymanager.repository.CustomerRepository;
import com.example.homestaymanager.repository.RoomRepository;
import com.example.homestaymanager.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional(readOnly = true)
    public StatisticsResponse getOverview(Integer occupancyYear, Integer occupancyMonth) {
        LocalDate today = LocalDate.now();
        YearMonth thisMonth = YearMonth.from(today);
        YearMonth occupancyMonthValue = resolveOccupancyMonth(occupancyYear, occupancyMonth, thisMonth);
        int thisYear = today.getYear();

        var bookings = bookingRepository.findAll();
        Map<String, BigDecimal> dailyRevenue = new LinkedHashMap<>();
        Map<String, BigDecimal> monthlyRevenue = new LinkedHashMap<>();
        Map<String, BigDecimal> yearlyRevenue = new LinkedHashMap<>();

        for (Booking booking : bookings) {
            if (!isRevenueBooking(booking)) {
                continue;
            }
            LocalDate date = booking.getUpdatedAt() != null
                    ? booking.getUpdatedAt().toLocalDate()
                    : booking.getCreatedAt() != null ? booking.getCreatedAt().toLocalDate() : today;
            BigDecimal amount = getPaidAmount(booking);

            dailyRevenue.merge(date.toString(), amount, BigDecimal::add);
            monthlyRevenue.merge(YearMonth.from(date).toString(), amount, BigDecimal::add);
            yearlyRevenue.merge(String.valueOf(date.getYear()), amount, BigDecimal::add);
        }

        var rooms = roomRepository.findVisibleRooms();
        var customers = customerRepository.findAll();
        var roomOccupancyThisMonth = buildRoomOccupancyThisMonth(rooms, bookings, occupancyMonthValue);

        return StatisticsResponse.builder()
                .revenueToday(dailyRevenue.getOrDefault(today.toString(), BigDecimal.ZERO))
                .revenueThisMonth(monthlyRevenue.getOrDefault(thisMonth.toString(), BigDecimal.ZERO))
                .revenueThisYear(yearlyRevenue.getOrDefault(String.valueOf(thisYear), BigDecimal.ZERO))
                .dailyRevenue(dailyRevenue)
                .monthlyRevenue(monthlyRevenue)
                .yearlyRevenue(yearlyRevenue)
                .totalRooms(rooms.size())
                .availableRooms(rooms.stream().filter(room -> room.getStatus() == null || room.getStatus() == RoomStatus.AVAILABLE).count())
                .waitingCheckInRooms(rooms.stream().filter(room -> room.getStatus() == RoomStatus.WAITING_CHECKIN).count())
                .occupiedRooms(rooms.stream().filter(room -> room.getStatus() == RoomStatus.OCCUPIED).count())
                .cleaningRooms(rooms.stream().filter(room -> room.getStatus() == RoomStatus.CLEANING).count())
                .maintenanceRooms(rooms.stream().filter(room -> room.getStatus() == RoomStatus.MAINTENANCE).count())
                .totalCustomers(customers.size())
                .activeCustomers(customers.stream().filter(customer -> customer.getStatus() == null || customer.getStatus() == CustomerStatus.ACTIVE).count())
                .lockedCustomers(customers.stream().filter(customer -> customer.getStatus() == CustomerStatus.LOCKED).count())
                .totalBookings(bookings.size())
                .bookingsByStatus(bookings.stream().collect(Collectors.groupingBy(
                        booking -> booking.getCurrentStatus() != null ? booking.getCurrentStatus().name() : "UNKNOWN",
                        LinkedHashMap::new,
                        Collectors.counting())))
                .roomOccupancyMonth(occupancyMonthValue.toString())
                .roomOccupancyThisMonth(roomOccupancyThisMonth)
                .build();
    }

    private boolean isRevenueBooking(Booking booking) {
        return booking.getCurrentStatus() == BookingStatus.CONFIRMED;
    }

    private YearMonth resolveOccupancyMonth(Integer year, Integer month, YearMonth fallback) {
        if (year == null || month == null || year < 2000 || month < 1 || month > 12) {
            return fallback;
        }
        return YearMonth.of(year, month);
    }

    private List<StatisticsResponse.RoomOccupancyRevenue> buildRoomOccupancyThisMonth(
            List<Room> rooms,
            List<Booking> bookings,
            YearMonth month) {
        LocalDate monthStart = month.atDay(1);
        LocalDate monthEndExclusive = month.plusMonths(1).atDay(1);
        long totalNights = month.lengthOfMonth();

        return rooms.stream()
                .map(room -> {
                    long occupiedNights = 0;
                    BigDecimal revenue = BigDecimal.ZERO;

                    for (Booking booking : bookings) {
                        if (!isOccupancyBooking(booking)
                                || booking.getRoom() == null
                                || booking.getRoom().getId() != room.getId()
                                || booking.getCheckIn() == null
                                || booking.getCheckOut() == null) {
                            continue;
                        }

                        long overlapNights = countOverlapNights(booking.getCheckIn(), booking.getCheckOut(), monthStart, monthEndExclusive);
                        if (overlapNights <= 0) {
                            continue;
                        }

                        occupiedNights += overlapNights;
                        revenue = revenue.add(calculateRevenueInMonth(booking, overlapNights));
                    }

                    long cappedOccupiedNights = Math.min(occupiedNights, totalNights);
                    BigDecimal occupancyRate = totalNights > 0
                            ? BigDecimal.valueOf(cappedOccupiedNights)
                                    .multiply(BigDecimal.valueOf(100))
                                    .divide(BigDecimal.valueOf(totalNights), 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;

                    return StatisticsResponse.RoomOccupancyRevenue.builder()
                            .roomId(room.getId())
                            .roomName(room.getName())
                            .occupiedNights(cappedOccupiedNights)
                            .totalNights(totalNights)
                            .occupancyRate(occupancyRate)
                            .revenue(revenue)
                            .build();
                })
                .sorted(Comparator.comparing(StatisticsResponse.RoomOccupancyRevenue::getRoomName,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .collect(Collectors.toList());
    }

    private long countOverlapNights(
            LocalDateTime checkIn,
            LocalDateTime checkOut,
            LocalDate monthStart,
            LocalDate monthEndExclusive) {
        LocalDate start = checkIn.toLocalDate().isAfter(monthStart) ? checkIn.toLocalDate() : monthStart;
        LocalDate end = checkOut.toLocalDate().isBefore(monthEndExclusive) ? checkOut.toLocalDate() : monthEndExclusive;
        return Math.max(0, ChronoUnit.DAYS.between(start, end));
    }

    private BigDecimal calculateRevenueInMonth(Booking booking, long overlapNights) {
        BigDecimal amount = getPaidAmount(booking);
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        long bookingNights = Math.max(1, ChronoUnit.DAYS.between(
                booking.getCheckIn().toLocalDate(),
                booking.getCheckOut().toLocalDate()));
        return amount
                .multiply(BigDecimal.valueOf(overlapNights))
                .divide(BigDecimal.valueOf(bookingNights), 0, RoundingMode.HALF_UP);
    }

    private BigDecimal getPaidAmount(Booking booking) {
        return booking.getPaidAmount() != null && booking.getPaidAmount().compareTo(BigDecimal.ZERO) > 0
                ? booking.getPaidAmount()
                : BigDecimal.ZERO;
    }

    private boolean isOccupancyBooking(Booking booking) {
        return booking.getCurrentStatus() == BookingStatus.PENDING
                || booking.getCurrentStatus() == BookingStatus.CONFIRMED;
    }
}
