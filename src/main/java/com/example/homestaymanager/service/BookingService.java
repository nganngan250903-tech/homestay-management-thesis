package com.example.homestaymanager.service;

import com.example.homestaymanager.dto.response.BookingResponse;
import com.example.homestaymanager.dto.response.BookingCalendarResponse;
import com.example.homestaymanager.dto.request.CreateBookingRequest;
import com.example.homestaymanager.enums.BookingStatus;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {

    BookingResponse createBooking(CreateBookingRequest request);

    BookingResponse getBookingById(int id);

    Page<BookingResponse> getBookings(Integer customerId, String customerName, Integer roomId, Integer branchId, BookingStatus status, LocalDate dateFrom, LocalDate dateTo, int page, int size);

    List<BookingCalendarResponse> getRoomBookingCalendar(int roomId, LocalDate dateFrom, LocalDate dateTo);

    BookingResponse updateStatus(int bookingId, BookingStatus newStatus);

    BookingResponse cancelBooking(int id);

    BookingResponse checkIn(int bookingId);

    BookingResponse checkOut(int bookingId);
}
