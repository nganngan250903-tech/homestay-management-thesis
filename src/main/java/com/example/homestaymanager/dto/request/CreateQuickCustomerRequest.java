package com.example.homestaymanager.dto.request;

import lombok.Data;

@Data
public class CreateQuickCustomerRequest {
    private String name;
    private String phone;
    private String email;
    private String address;
}
