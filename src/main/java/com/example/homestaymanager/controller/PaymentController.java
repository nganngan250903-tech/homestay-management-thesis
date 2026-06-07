package com.example.homestaymanager.controller;

import com.example.homestaymanager.constant.ApiMessage;
import com.example.homestaymanager.constant.ApiStatus;
import com.example.homestaymanager.dto.response.ApiResponse;
import com.example.homestaymanager.dto.response.BookingResponse;
import com.example.homestaymanager.dto.response.PaymentTransactionResponse;
import com.example.homestaymanager.dto.response.PaymentUrlResponse;
import com.example.homestaymanager.enums.PaymentTransactionStatus;
import com.example.homestaymanager.exception.UnauthorizedException;
import com.example.homestaymanager.model.Booking;
import com.example.homestaymanager.model.PaymentTransaction;
import com.example.homestaymanager.model.Room;
import com.example.homestaymanager.repository.PaymentTransactionRepository;
import com.example.homestaymanager.security.SecurityUtil;
import com.example.homestaymanager.service.BookingService;
import com.example.homestaymanager.service.VnPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final BookingService bookingService;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final VnPayService vnPayService;

    @GetMapping("/payments")
    public ApiResponse<Page<PaymentTransactionResponse>> getPayments(
            @RequestParam(required = false) PaymentTransactionStatus status,
            @RequestParam(required = false) String provider,
            @RequestParam(required = false) Integer bookingId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (SecurityUtil.isCustomer()) {
            throw new UnauthorizedException("Khách hàng không thể xem danh sách thanh toán");
        }
        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = 20;
        }
        String normalizedProvider = provider != null && !provider.isBlank() ? provider.trim() : null;
        Page<PaymentTransactionResponse> data = paymentTransactionRepository
                .findByFilters(status, normalizedProvider, bookingId, PageRequest.of(page, size))
                .map(PaymentController::toResponse);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.SUCCESS, data);
    }

    @PostMapping("/payments/vnpay/create/{bookingId}")
    public ApiResponse<PaymentUrlResponse> createVnPayUrl(@PathVariable int bookingId, HttpServletRequest request) {
        if (SecurityUtil.isCustomer()) {
            var booking = bookingService.getBookingById(bookingId);
            var current = SecurityUtil.getCurrentUser();
            if (current != null && booking.getCustomerId() != current.getId()) {
                throw new UnauthorizedException("Bạn chỉ có thể thanh toán booking của chính mình");
            }
        }

        return ApiResponse.of(ApiStatus.OK, ApiMessage.SUCCESS, vnPayService.createPaymentUrl(bookingId, getClientIp(request)));
    }

    @PostMapping("/payments/demo-success/{bookingId}")
    public ApiResponse<BookingResponse> confirmDemoPayment(@PathVariable int bookingId) {
        if (SecurityUtil.isCustomer()) {
            var booking = bookingService.getBookingById(bookingId);
            var current = SecurityUtil.getCurrentUser();
            if (current != null && booking.getCustomerId() != current.getId()) {
                throw new UnauthorizedException("Bạn chỉ có thể thanh toán booking của chính mình");
            }
        }

        return ApiResponse.of(ApiStatus.OK, ApiMessage.SUCCESS, vnPayService.confirmDemoPayment(bookingId));
    }

    @PostMapping("/payments/vnpay/confirm-return")
    public ApiResponse<BookingResponse> confirmVnPayReturn(@RequestBody Map<String, String> params) {
        String bookingIdValue = params != null ? params.get("vnp_TxnRef") : null;
        if (SecurityUtil.isCustomer() && bookingIdValue != null) {
            int bookingId;
            try {
                bookingId = Integer.parseInt(bookingIdValue);
            } catch (NumberFormatException exception) {
                throw new RuntimeException("Mã booking từ VNPay không hợp lệ");
            }
            var booking = bookingService.getBookingById(bookingId);
            var current = SecurityUtil.getCurrentUser();
            if (current != null && booking.getCustomerId() != current.getId()) {
                throw new UnauthorizedException("Bạn chỉ có thể thanh toán booking của chính mình");
            }
        }

        return ApiResponse.of(ApiStatus.OK, ApiMessage.SUCCESS, vnPayService.confirmReturnPayment(params));
    }

    private static String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static PaymentTransactionResponse toResponse(PaymentTransaction transaction) {
        Booking booking = transaction.getBooking();
        Room room = booking != null ? booking.getRoom() : null;
        String roomName = null;
        if (room != null) {
            roomName = room.getName() != null && !room.getName().isBlank()
                    ? room.getName().trim()
                    : "Phòng";
        }
        return PaymentTransactionResponse.builder()
                .id(transaction.getId())
                .provider(transaction.getProvider())
                .txnRef(transaction.getTxnRef())
                .providerTransactionNo(transaction.getProviderTransactionNo())
                .responseCode(transaction.getResponseCode())
                .message(transaction.getMessage())
                .amount(transaction.getAmount())
                .status(transaction.getStatus())
                .paidAt(transaction.getPaidAt())
                .createdAt(transaction.getCreatedAt())
                .bookingId(booking != null ? booking.getId() : null)
                .bookingStatus(booking != null ? booking.getCurrentStatus() : null)
                .customerName(booking != null && booking.getCustomer() != null ? booking.getCustomer().getName() : null)
                .roomName(roomName)
                .roomTypeName(room != null && room.getRoomType() != null ? room.getRoomType().getName() : null)
                .bookingTotalAmount(booking != null ? booking.getTotalAmount() : null)
                .bookingPaidAmount(booking != null ? booking.getPaidAmount() : null)
                .build();
    }
}
