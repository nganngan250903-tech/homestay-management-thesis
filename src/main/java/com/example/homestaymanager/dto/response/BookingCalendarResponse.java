package com.example.homestaymanager.dto.response;

import com.example.homestaymanager.enums.BookingStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BookingCalendarResponse {

    private LocalDateTime checkIn;

    private LocalDateTime checkOut;

    private BookingStatus status;
}
