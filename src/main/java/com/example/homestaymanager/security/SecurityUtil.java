package com.example.homestaymanager.security;

import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    public static AuthenticatedUser getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof AuthenticatedUser) {
            return (AuthenticatedUser) principal;
        }
        return null;
    }

    public static boolean isAdmin() {
        AuthenticatedUser user = getCurrentUser();
        return user != null && user.isAdmin();
    }

    public static boolean isEmployee() {
        AuthenticatedUser user = getCurrentUser();
        return user != null && user.isEmployee();
    }

    public static boolean isCustomer() {
        AuthenticatedUser user = getCurrentUser();
        return user != null && user.isCustomer();
    }
}
