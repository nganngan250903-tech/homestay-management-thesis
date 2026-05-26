package com.example.homestaymanager.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateBookingRequest {
    private int customerId;
    private String customerKeyword;
    private Integer employeeId;
    private int roomId;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private int guestCount;
}
