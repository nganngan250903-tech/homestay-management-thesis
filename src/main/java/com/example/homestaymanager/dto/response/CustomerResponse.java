package com.example.homestaymanager.dto.response;

import com.example.homestaymanager.enums.AuthProvider;
import com.example.homestaymanager.enums.CustomerStatus;
import lombok.Data;

@Data
public class CustomerResponse {

    private int id;
    private String email;
    private String name;
    private String phone;
    private String address;
    private String image;
    private CustomerStatus status;
    private AuthProvider provider;
}
