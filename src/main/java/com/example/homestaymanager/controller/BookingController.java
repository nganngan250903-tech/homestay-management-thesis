package com.example.homestaymanager.controller;

import com.example.homestaymanager.constant.ApiMessage;
import com.example.homestaymanager.constant.ApiStatus;
import com.example.homestaymanager.dto.request.CreateBookingRequest;
import com.example.homestaymanager.dto.request.UpdateBookingStatusRequest;
import com.example.homestaymanager.dto.response.ApiResponse;
import com.example.homestaymanager.dto.response.BookingCalendarResponse;
import com.example.homestaymanager.dto.response.BookingResponse;
import com.example.homestaymanager.enums.BookingStatus;
import com.example.homestaymanager.exception.UnauthorizedException;
import com.example.homestaymanager.security.SecurityUtil;
import com.example.homestaymanager.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping("/bookings")
    public ApiResponse<Page<BookingResponse>> getBookings(
            @RequestParam(required = false) Integer customerId,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) Integer roomId,
            @RequestParam(required = false) Integer branchId,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (SecurityUtil.isCustomer()) {
            var current = SecurityUtil.getCurrentUser();
            if (current != null && customerId == null) {
                customerId = current.getId();
            } else if (current != null && customerId != null && !Objects.equals(customerId, current.getId())) {
                throw new UnauthorizedException("Không có quyền xem booking của khách hàng khác");
            }
        }

        Page<BookingResponse> data = bookingService.getBookings(
                customerId,
                customerName,
                roomId,
                branchId,
                status,
                dateFrom,
                dateTo,
                page,
                size);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.SUCCESS, data);
    }

    @GetMapping("/rooms/{roomId}/booking-calendar")
    public ApiResponse<List<BookingCalendarResponse>> getRoomBookingCalendar(
            @PathVariable int roomId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        List<BookingCalendarResponse> data = bookingService.getRoomBookingCalendar(roomId, dateFrom, dateTo);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.SUCCESS, data);
    }

    @PostMapping("/bookings")
    public ApiResponse<BookingResponse> createBooking(@RequestBody CreateBookingRequest request) {
        var current = SecurityUtil.getCurrentUser();
        if (!SecurityUtil.isCustomer() && !SecurityUtil.isEmployee()) {
            throw new UnauthorizedException("Không có quyền tạo booking");
        }
        if (SecurityUtil.isCustomer() && current != null) {
            request.setCustomerId(current.getId());
        }
        if (SecurityUtil.isCustomer() && current != null && !Objects.equals(request.getCustomerId(), current.getId())) {
            throw new UnauthorizedException("Bạn chỉ có thể tạo booking cho chính mình");
        }
        if (SecurityUtil.isEmployee() && current != null) {
            request.setEmployeeId(current.getId());
        }

        BookingResponse data = bookingService.createBooking(request);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.CREATED, data);
    }

    @GetMapping("/bookings/{bookingId}")
    public ApiResponse<BookingResponse> getBookingById(@PathVariable int bookingId) {
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

    @PostMapping("/bookings/{bookingId}/check-in")
    public ApiResponse<BookingResponse> checkIn(@PathVariable int bookingId) {
        if (SecurityUtil.isCustomer()) {
            throw new UnauthorizedException("Khách hàng không thể check-in booking");
        }
        return ApiResponse.of(ApiStatus.OK, ApiMessage.SUCCESS, bookingService.checkIn(bookingId));
    }

    @PostMapping("/bookings/{bookingId}/check-out")
    public ApiResponse<BookingResponse> checkOut(@PathVariable int bookingId) {
        if (SecurityUtil.isCustomer()) {
            throw new UnauthorizedException("Khách hàng không thể check-out booking");
        }
        return ApiResponse.of(ApiStatus.OK, ApiMessage.SUCCESS, bookingService.checkOut(bookingId));
    }
}
