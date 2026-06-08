package com.example.homestaymanager.service;

import com.example.homestaymanager.model.Booking;

public interface BookingEmailService {
    boolean sendBookingConfirmedEmail(Booking booking);

    boolean sendCheckInReminderEmail(Booking booking);
}
