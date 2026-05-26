package com.example.homestaymanager.service;

import com.example.homestaymanager.dto.response.BookingResponse;
import com.example.homestaymanager.dto.response.PaymentUrlResponse;
import com.example.homestaymanager.enums.BookingStatus;
import com.example.homestaymanager.enums.PaymentTransactionStatus;
import com.example.homestaymanager.enums.RoomStatus;
import com.example.homestaymanager.model.Booking;
import com.example.homestaymanager.model.PaymentTransaction;
import com.example.homestaymanager.repository.BookingRepository;
import com.example.homestaymanager.repository.PaymentTransactionRepository;
import com.example.homestaymanager.service.impl.BookingServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class VnPayService {

    private static final DateTimeFormatter DEMO_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final BookingService bookingService;
    private final BookingRepository bookingRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;

    @Value("${vnpay.pay-url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String payUrl;

    @Value("${vnpay.tmn-code:}")
    private String tmnCode;

    @Value("${vnpay.hash-secret:}")
    private String hashSecret;

    @Value("${vnpay.return-url:http://localhost:5173/home/payment-result}")
    private String returnUrl;

    public PaymentUrlResponse createPaymentUrl(int bookingId, String clientIp) {
        BookingResponse booking = bookingService.getBookingById(bookingId);
        if (booking.getCurrentStatus() != BookingStatus.PENDING && booking.getCurrentStatus() != BookingStatus.CONFIRMED) {
            throw new RuntimeException("Booking không ở trạng thái có thể thanh toán");
        }
        if (booking.getTotalAmount() == null || booking.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Booking chưa có số tiền cần thanh toán");
        }

        if (isBlank(tmnCode) || isBlank(hashSecret)) {
            return PaymentUrlResponse.builder()
                    .demoMode(true)
                    .paymentUrl(returnUrl + "?vnp_ResponseCode=00&vnp_TxnRef=" + bookingId + "&demo=true")
                    .message("Chưa cấu hình VNPAY_TMN_CODE/VNPAY_HASH_SECRET, dùng thanh toán demo nội bộ.")
                    .build();
        }

        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", booking.getTotalAmount().multiply(BigDecimal.valueOf(100)).toBigInteger().toString());
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", String.valueOf(booking.getId()));
        params.put("vnp_OrderInfo", "Thanh toan booking #" + booking.getId());
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", isBlank(clientIp) ? "127.0.0.1" : clientIp);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        params.put("vnp_CreateDate", formatter.format(new Date()));
        params.put("vnp_ExpireDate", LocalDateTime.now().plusMinutes(15).format(DEMO_TIME_FORMAT));

        String hashData = buildQuery(params, true);
        String secureHash = hmacSha512(hashSecret, hashData);
        String paymentUrl = payUrl + "?" + buildQuery(params, true) + "&vnp_SecureHash=" + secureHash;

        return PaymentUrlResponse.builder()
                .demoMode(false)
                .paymentUrl(paymentUrl)
                .message("Đã tạo URL thanh toán VNPay sandbox.")
                .build();
    }

    @Transactional
    public BookingResponse confirmDemoPayment(int bookingId) {
        var booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại"));
        confirmPaidBooking(booking);
        savePaymentTransaction(booking, "DEMO", String.valueOf(bookingId), null, "00",
                "Thanh toan demo thanh cong", booking.getTotalAmount(), PaymentTransactionStatus.SUCCESS, null, LocalDateTime.now());
        return BookingServiceImpl.toResponse(booking);
    }

    @Transactional
    public BookingResponse confirmReturnPayment(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            throw new RuntimeException("Thiếu dữ liệu kết quả thanh toán");
        }

        String txnRef = params.get("vnp_TxnRef");
        if (isBlank(txnRef)) {
            throw new RuntimeException("Thiếu mã booking từ VNPay");
        }

        int bookingId;
        try {
            bookingId = Integer.parseInt(txnRef);
        } catch (NumberFormatException exception) {
            throw new RuntimeException("Mã booking từ VNPay không hợp lệ");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại"));

        boolean demoPayment = "true".equalsIgnoreCase(params.get("demo"));
        if (!demoPayment) {
            verifyReturnSignature(params);
        }

        String responseCode = params.get("vnp_ResponseCode");
        BigDecimal amount = parseVnPayAmount(params.get("vnp_Amount"), booking.getTotalAmount());
        PaymentTransactionStatus transactionStatus = "00".equals(responseCode)
                ? PaymentTransactionStatus.SUCCESS
                : PaymentTransactionStatus.FAILED;

        savePaymentTransaction(
                booking,
                demoPayment ? "DEMO" : "VNPAY",
                txnRef,
                params.get("vnp_TransactionNo"),
                responseCode,
                params.get("vnp_Message"),
                amount,
                transactionStatus,
                params.get("vnp_SecureHash"),
                parseVnPayDate(params.get("vnp_PayDate")));

        if (transactionStatus != PaymentTransactionStatus.SUCCESS) {
            return BookingServiceImpl.toResponse(booking);
        }

        if (amount.compareTo(booking.getTotalAmount()) != 0) {
            throw new RuntimeException("Số tiền thanh toán không khớp với booking");
        }

        confirmPaidBooking(booking);
        return BookingServiceImpl.toResponse(booking);
    }

    private void confirmPaidBooking(Booking booking) {
        BookingStatus status = booking.getCurrentStatus();
        if (status != BookingStatus.PENDING && status != BookingStatus.CONFIRMED) {
            throw new RuntimeException("Booking không ở trạng thái có thể xác nhận thanh toán");
        }
        booking.setPaidAmount(booking.getTotalAmount());
        booking.setCurrentStatus(BookingStatus.CONFIRMED);
        LocalDateTime now = LocalDateTime.now();
        if (!booking.getCheckIn().isAfter(now)
                && booking.getCheckOut().isAfter(now)
                && booking.getActualCheckInAt() == null
                && booking.getActualCheckOutAt() == null
                && (booking.getRoom().getStatus() == null || booking.getRoom().getStatus() == RoomStatus.AVAILABLE)) {
            booking.getRoom().setStatus(RoomStatus.WAITING_CHECKIN);
        }
    }

    private void savePaymentTransaction(
            Booking booking,
            String provider,
            String txnRef,
            String providerTransactionNo,
            String responseCode,
            String message,
            BigDecimal amount,
            PaymentTransactionStatus status,
            String secureHash,
            LocalDateTime paidAt) {
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setBooking(booking);
        transaction.setProvider(provider);
        transaction.setTxnRef(txnRef);
        transaction.setProviderTransactionNo(providerTransactionNo);
        transaction.setResponseCode(responseCode);
        transaction.setMessage(message);
        transaction.setAmount(amount);
        transaction.setStatus(status);
        transaction.setSecureHash(secureHash);
        transaction.setPaidAt(paidAt);
        paymentTransactionRepository.save(transaction);
    }

    private void verifyReturnSignature(Map<String, String> params) {
        if (isBlank(hashSecret)) {
            return;
        }
        String secureHash = params.get("vnp_SecureHash");
        if (isBlank(secureHash)) {
            throw new RuntimeException("Thiếu chữ ký thanh toán VNPay");
        }

        Map<String, String> signedParams = new HashMap<>(params);
        signedParams.remove("vnp_SecureHash");
        signedParams.remove("vnp_SecureHashType");

        String hashData = buildQuery(new TreeMap<>(signedParams), true);
        String expectedHash = hmacSha512(hashSecret, hashData);
        if (!expectedHash.equalsIgnoreCase(secureHash)) {
            throw new RuntimeException("Chữ ký thanh toán VNPay không hợp lệ");
        }
    }

    private static BigDecimal parseVnPayAmount(String value, BigDecimal fallback) {
        if (isBlank(value)) {
            return fallback;
        }
        return new BigDecimal(value).divide(BigDecimal.valueOf(100));
    }

    private static LocalDateTime parseVnPayDate(String value) {
        if (isBlank(value)) {
            return LocalDateTime.now();
        }
        return LocalDateTime.parse(value, DEMO_TIME_FORMAT);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String buildQuery(Map<String, String> params, boolean encodeValue) {
        StringBuilder query = new StringBuilder();
        params.forEach((key, value) -> {
            if (query.length() > 0) {
                query.append('&');
            }
            query.append(urlEncode(key));
            query.append('=');
            query.append(encodeValue ? urlEncode(value) : value);
        });
        return query.toString();
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.US_ASCII);
    }

    private static String hmacSha512(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac.init(secretKey);
            byte[] bytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hash = new StringBuilder(bytes.length * 2);
            for (byte item : bytes) {
                hash.append(String.format("%02x", item));
            }
            return hash.toString();
        } catch (Exception exception) {
            throw new RuntimeException("Không thể tạo chữ ký VNPay", exception);
        }
    }
}
