package com.example.homestaymanager.service;

import com.example.homestaymanager.dto.request.LoginRequest;
import com.example.homestaymanager.dto.request.RegisterRequest;
import com.example.homestaymanager.dto.response.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);

    LoginResponse registerCustomer(RegisterRequest request);
}
