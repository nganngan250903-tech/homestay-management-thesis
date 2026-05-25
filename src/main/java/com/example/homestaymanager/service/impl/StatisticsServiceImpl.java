package com.example.homestaymanager.service.impl;

import com.example.homestaymanager.dto.response.StatisticsResponse;
import com.example.homestaymanager.enums.BookingStatus;
import com.example.homestaymanager.enums.CustomerStatus;
import com.example.homestaymanager.enums.RoomStatus;
import com.example.homestaymanager.model.Booking;
import com.example.homestaymanager.repository.BookingRepository;
import com.example.homestaymanager.repository.CustomerRepository;
import com.example.homestaymanager.repository.RoomRepository;
import com.example.homestaymanager.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
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
    public StatisticsResponse getOverview() {
        LocalDate today = LocalDate.now();
        YearMonth thisMonth = YearMonth.from(today);
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
            BigDecimal amount = booking.getPaidAmount() != null && booking.getPaidAmount().compareTo(BigDecimal.ZERO) > 0
                    ? booking.getPaidAmount()
                    : booking.getTotalAmount();
            if (amount == null) {
                amount = BigDecimal.ZERO;
            }

            dailyRevenue.merge(date.toString(), amount, BigDecimal::add);
            monthlyRevenue.merge(YearMonth.from(date).toString(), amount, BigDecimal::add);
            yearlyRevenue.merge(String.valueOf(date.getYear()), amount, BigDecimal::add);
        }

        var rooms = roomRepository.findAll();
        var customers = customerRepository.findAll();

        return StatisticsResponse.builder()
                .revenueToday(dailyRevenue.getOrDefault(today.toString(), BigDecimal.ZERO))
                .revenueThisMonth(monthlyRevenue.getOrDefault(thisMonth.toString(), BigDecimal.ZERO))
                .revenueThisYear(yearlyRevenue.getOrDefault(String.valueOf(thisYear), BigDecimal.ZERO))
                .dailyRevenue(dailyRevenue)
                .monthlyRevenue(monthlyRevenue)
                .yearlyRevenue(yearlyRevenue)
                .totalRooms(rooms.size())
                .availableRooms(rooms.stream().filter(room -> room.getStatus() == null || room.getStatus() == RoomStatus.AVAILABLE).count())
                .occupiedRooms(rooms.stream().filter(room -> room.getStatus() == RoomStatus.OCCUPIED).count())
                .totalCustomers(customers.size())
                .activeCustomers(customers.stream().filter(customer -> customer.getStatus() == null || customer.getStatus() == CustomerStatus.ACTIVE).count())
                .lockedCustomers(customers.stream().filter(customer -> customer.getStatus() == CustomerStatus.LOCKED).count())
                .totalBookings(bookings.size())
                .bookingsByStatus(bookings.stream().collect(Collectors.groupingBy(
                        booking -> booking.getCurrentStatus().name(),
                        LinkedHashMap::new,
                        Collectors.counting())))
                .build();
    }

    private boolean isRevenueBooking(Booking booking) {
        return booking.getCurrentStatus() == BookingStatus.CONFIRMED;
    }
}
