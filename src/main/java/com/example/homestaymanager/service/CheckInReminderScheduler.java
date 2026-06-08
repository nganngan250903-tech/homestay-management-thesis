package com.example.homestaymanager.service;

import com.example.homestaymanager.enums.BookingStatus;
import com.example.homestaymanager.model.Booking;
import com.example.homestaymanager.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CheckInReminderScheduler {

    private final BookingRepository bookingRepository;
    private final BookingEmailService bookingEmailService;

    @Scheduled(cron = "${app.mail.check-in-reminder-cron:0 0 12 * * *}", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public void sendDailyCheckInReminders() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime startOfTomorrow = today.plusDays(1).atStartOfDay();

        List<Booking> bookings =
                bookingRepository.findByCurrentStatusAndCheckInGreaterThanEqualAndCheckInBeforeAndActualCheckInAtIsNullAndHasSentReminderFalse(
                        BookingStatus.CONFIRMED,
                        startOfToday,
                        startOfTomorrow);

        if (bookings.isEmpty()) {
            System.out.println("Cron nhắc check-in đã chạy: không có booking cần gửi gmail");
            return;
        }

        bookings.forEach((booking) -> {
            if (bookingEmailService.sendCheckInReminderEmail(booking)) {
                booking.setHasSentReminder(true);
            }
        });
    }
}
