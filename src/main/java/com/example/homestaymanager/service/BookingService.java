package com.example.homestaymanager.service;

import com.example.homestaymanager.dto.response.BookingResponse;
import com.example.homestaymanager.dto.request.CreateBookingRequest;
import com.example.homestaymanager.enums.BookingStatus;
import org.springframework.data.domain.Page;

public interface BookingService {

    BookingResponse createBooking(CreateBookingRequest request);

    BookingResponse getBookingById(int id);

    Page<BookingResponse> getBookings(Integer customerId, Integer roomId, Integer branchId, BookingStatus status, int page, int size);

    BookingResponse updateStatus(int bookingId, BookingStatus newStatus);

    BookingResponse cancelBooking(int id);
}
