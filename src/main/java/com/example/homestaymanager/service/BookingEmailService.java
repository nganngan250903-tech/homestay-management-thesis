package com.example.homestaymanager.service;

import com.example.homestaymanager.model.Booking;

public interface BookingEmailService {
    void sendBookingConfirmedEmail(Booking booking);
}
