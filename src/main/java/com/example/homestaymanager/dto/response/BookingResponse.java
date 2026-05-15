package com.example.homestaymanager.dto.response;

import com.example.homestaymanager.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private int id;
    private int customerId;
    private String customerName;
    private Integer employeeId;
    private int roomId;
    private Integer branchId;
    private String roomTypeName;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private int guestCount;
    private BookingStatus currentStatus;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private boolean hasSentReminder;
    private Integer refundPercentage;
    private LocalDateTime pendingExpiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
