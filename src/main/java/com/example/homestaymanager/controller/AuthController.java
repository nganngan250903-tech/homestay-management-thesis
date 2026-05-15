package com.example.homestaymanager.controller;

import com.example.homestaymanager.constant.ApiStatus;
import com.example.homestaymanager.dto.request.LoginRequest;
import com.example.homestaymanager.dto.request.RegisterRequest;
import com.example.homestaymanager.dto.response.ApiResponse;
import com.example.homestaymanager.dto.response.LoginResponse;
import com.example.homestaymanager.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/auth/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        return ApiResponse.of(ApiStatus.OK, "Login successfully", authService.login(request));
    }

    @PostMapping("/auth/customer/register")
    public ApiResponse<LoginResponse> registerCustomer(@RequestBody RegisterRequest request) {
        return ApiResponse.of(ApiStatus.OK, "Register successfully", authService.registerCustomer(request));
    }
}
