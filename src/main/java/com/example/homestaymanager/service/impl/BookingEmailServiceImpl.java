package com.example.homestaymanager.service.impl;

import com.example.homestaymanager.model.Booking;
import com.example.homestaymanager.model.Branch;
import com.example.homestaymanager.model.Customer;
import com.example.homestaymanager.model.Room;
import com.example.homestaymanager.service.BookingEmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingEmailServiceImpl implements BookingEmailService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String HOMESTAY_NAME = "LimDimHomestay";
    private static final String HOMESTAY_ADDRESS = "16/52 Bà Triệu, Thuận Hóa.";
    private static final String HOMESTAY_PHONE = "0343883688";

    private final JavaMailSender mailSender;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.mail.from:}")
    private String mailFrom;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Override
    public void sendBookingConfirmedEmail(Booking booking) {
        if (!mailEnabled) {
            log.info("Skip booking confirmation email because MAIL_ENABLED is false");
            return;
        }
        if (booking == null || booking.getCustomer() == null || isBlank(booking.getCustomer().getEmail())) {
            log.warn("Skip booking confirmation email because booking customer email is missing");
            return;
        }
        String from = isBlank(mailFrom) ? mailUsername : mailFrom;
        if (isBlank(from)) {
            log.warn("Skip booking confirmation email because MAIL_FROM/MAIL_USERNAME is missing");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(booking.getCustomer().getEmail());
            helper.setSubject("Xác nhận đặt phòng thành công");
            helper.setText(buildEmailHtml(booking), true);
            mailSender.send(message);
        } catch (Exception exception) {
            log.warn("Cannot send booking confirmation email for booking {}", booking.getId(), exception);
        }
    }

    private String buildEmailHtml(Booking booking) {
        Customer customer = booking.getCustomer();
        Room room = booking.getRoom();

        String roomName = room != null ? valueOrDefault(room.getName(), "Đang cập nhật") : "Đang cập nhật";
        String guestCount = String.valueOf(booking.getGuestCount());

        return """
                <!doctype html>
                <html lang="vi">
                <body style="margin:0;padding:0;background:#f6fbff;font-family:Arial,sans-serif;color:#1f2937;">
                  <div style="max-width:640px;margin:0 auto;padding:24px;">
                    <div style="background:#ffffff;border:1px solid #dbeafe;border-radius:12px;overflow:hidden;">
                      <div style="background:#dcfce7;padding:18px 24px;">
                        <h2 style="margin:0;color:#166534;font-size:22px;">Xác nhận đặt phòng thành công</h2>
                      </div>
                      <div style="padding:24px;">
                        <p style="margin:0 0 16px;">Xin chào %s,</p>
                        <p style="margin:0 0 20px;">Cảm ơn bạn đã đặt phòng. Thông tin đặt phòng của bạn đã được xác nhận.</p>
                        <table style="width:100%%;border-collapse:collapse;font-size:15px;">
                          %s
                          %s
                          %s
                          %s
                          %s
                          %s
                          %s
                        </table>
                        <p style="margin:22px 0 0;">Nếu cần hỗ trợ, vui lòng liên hệ số điện thoại của homestay.</p>
                      </div>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(
                escapeHtml(valueOrDefault(customer.getName(), "quý khách")),
                row("Tên homestay", HOMESTAY_NAME),
                row("Địa chỉ homestay", HOMESTAY_ADDRESS),
                row("Tên phòng", roomName),
                row("Số lượng người", guestCount),
                row("Ngày check-in", formatDateTime(booking.getCheckIn())),
                row("Ngày check-out", formatDateTime(booking.getCheckOut())),
                row("SĐT liên hệ", HOMESTAY_PHONE)
        );
    }

    private static String row(String label, String value) {
        return """
                <tr>
                  <td style="width:38%%;padding:12px 10px;border-top:1px solid #e5e7eb;color:#475569;font-weight:600;">%s</td>
                  <td style="padding:12px 10px;border-top:1px solid #e5e7eb;">%s</td>
                </tr>
                """.formatted(escapeHtml(label), escapeHtml(valueOrDefault(value, "Đang cập nhật")));
    }

    private static String formatDateTime(LocalDateTime value) {
        return value == null ? "Đang cập nhật" : value.format(DATE_TIME_FORMATTER);
    }

    private static String valueOrDefault(String value, String fallback) {
        return isBlank(value) ? fallback : value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
