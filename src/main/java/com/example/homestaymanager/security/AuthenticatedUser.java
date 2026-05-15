package com.example.homestaymanager.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticatedUser {
    private int id;
    private String email;
    private String userType;
    private String role;

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    public boolean isEmployee() {
        return "EMPLOYEE".equalsIgnoreCase(role) || isAdmin();
    }

    public boolean isCustomer() {
        return "CUSTOMER".equalsIgnoreCase(role);
    }
}
