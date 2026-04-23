package com.example.homestaymanager.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiResponse<T> {
    private int status;
    private String message;
    private T data;

    public static <T> ApiResponse<T> of(int status, String message,  T data) {
        return ApiResponse.<T>builder()
                .status(status)
                .message(message)
                .data(data)
                .build();
    }
}