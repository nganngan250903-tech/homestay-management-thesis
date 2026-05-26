package com.example.homestaymanager.dto.response;

import com.example.homestaymanager.enums.BookingStatus;
import com.example.homestaymanager.enums.PaymentTransactionStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentTransactionResponse {
    private int id;
    private String provider;
    private String txnRef;
    private String providerTransactionNo;
    private String responseCode;
    private String message;
    private BigDecimal amount;
    private PaymentTransactionStatus status;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private Integer bookingId;
    private BookingStatus bookingStatus;
    private String customerName;
    private String roomName;
    private String roomTypeName;
    private BigDecimal bookingTotalAmount;
    private BigDecimal bookingPaidAmount;
}
