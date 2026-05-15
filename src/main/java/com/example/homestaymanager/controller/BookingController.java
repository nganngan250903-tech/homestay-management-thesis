package com.example.homestaymanager.controller;

import com.example.homestaymanager.constant.ApiMessage;
import com.example.homestaymanager.constant.ApiStatus;
import com.example.homestaymanager.dto.request.CreateBookingRequest;
import com.example.homestaymanager.dto.request.UpdateBookingStatusRequest;
import com.example.homestaymanager.dto.response.ApiResponse;
import com.example.homestaymanager.dto.response.BookingResponse;
import com.example.homestaymanager.enums.BookingStatus;
import com.example.homestaymanager.exception.UnauthorizedException;
import com.example.homestaymanager.security.SecurityUtil;
import com.example.homestaymanager.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping("/bookings")
    public ApiResponse<Page<BookingResponse>> getBookings(
            @RequestParam(required = false) Integer customerId,
            @RequestParam(required = false) Integer roomId,
            @RequestParam(required = false) Integer branchId,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        // Khách hàng chỉ xem booking của chính mình
        if (SecurityUtil.isCustomer()) {
            var current = SecurityUtil.getCurrentUser();
            if (current != null && customerId == null) {
                customerId = current.getId();
            } else if (current != null && customerId != null && customerId != current.getId()) {
                throw new UnauthorizedException("Không có quyền xem booking của khách hàng khác");
            }
        }
        // Employee và Admin có thể xem tất cả
        Page<BookingResponse> data = bookingService.getBookings(customerId, roomId, branchId, status, page, size);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.SUCCESS, data);
    }

    @PostMapping("/bookings")
    public ApiResponse<BookingResponse> createBooking(@RequestBody CreateBookingRequest request) {
        // Chỉ Customer có thể tạo booking
        if (!SecurityUtil.isCustomer()) {
            throw new UnauthorizedException("Chỉ khách hàng có thể tạo đặt phòng");
        }
        var current = SecurityUtil.getCurrentUser();
        if (current != null && request.getCustomerId() != current.getId()) {
            throw new UnauthorizedException("Bạn chỉ có thể tạo booking cho chính mình");
        }
        BookingResponse data = bookingService.createBooking(request);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.CREATED, data);
    }

    @GetMapping("/bookings/{bookingId}")
    public ApiResponse<BookingResponse> getBookingById(@PathVariable int bookingId) {
        // Khách hàng chỉ xem booking của chính mình
        if (SecurityUtil.isCustomer()) {
            BookingResponse booking = bookingService.getBookingById(bookingId);
            var current = SecurityUtil.getCurrentUser();
            if (current != null && booking.getCustomerId() != current.getId()) {
                throw new UnauthorizedException("Không có quyền xem booking này");
            }
        }
        return ApiResponse.of(ApiStatus.OK, ApiMessage.SUCCESS, bookingService.getBookingById(bookingId));
    }

    @PatchMapping("/bookings/{bookingId}/status")
    public ApiResponse<BookingResponse> updateStatus(@PathVariable int bookingId, @RequestBody UpdateBookingStatusRequest body) {
        // Chỉ Employee và Admin có thể cập nhật trạng thái
        if (SecurityUtil.isCustomer()) {
            throw new UnauthorizedException("Khách hàng không thể cập nhật trạng thái booking");
        }
        if (body == null) {
            return ApiResponse.of(ApiStatus.BAD_REQUEST, "Trạng thái truyền lên không được để trống", null);
        }
        BookingResponse data = bookingService.updateStatus(bookingId, body.getStatus());
        return ApiResponse.of(ApiStatus.OK, ApiMessage.SUCCESS, data);
    }

    @PostMapping("/bookings/{bookingId}/cancel")
    public ApiResponse<BookingResponse> cancelBooking(@PathVariable int bookingId) {
        // Khách hàng chỉ hủy booking của chính mình, Employee và Admin có thể hủy tất cả
        if (SecurityUtil.isCustomer()) {
            BookingResponse booking = bookingService.getBookingById(bookingId);
            var current = SecurityUtil.getCurrentUser();
            if (current != null && booking.getCustomerId() != current.getId()) {
                throw new UnauthorizedException("Bạn chỉ có thể hủy booking của chính mình");
            }
        }
        BookingResponse data = bookingService.cancelBooking(bookingId);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.SUCCESS, data);
    }
}
