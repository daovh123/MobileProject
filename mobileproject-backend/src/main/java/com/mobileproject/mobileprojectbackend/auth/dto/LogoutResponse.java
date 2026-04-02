package com.mobileproject.mobileprojectbackend.auth.dto;

public record LogoutResponse(
        boolean success,
        String message
) {
    public static LogoutResponse success(String message) {
        return new LogoutResponse(true, message);
    }

    public static LogoutResponse failure(String message) {
        return new LogoutResponse(false, message);
    }
}