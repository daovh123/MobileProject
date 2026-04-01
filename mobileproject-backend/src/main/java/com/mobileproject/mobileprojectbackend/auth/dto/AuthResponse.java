package com.mobileproject.mobileprojectbackend.auth.dto;

public record AuthResponse(
        boolean success,
        String message,
        String token,
        String username,
        String email
) {
    public static AuthResponse success(String message, String token, String username, String email) {
        return new AuthResponse(true, message, token, username, email);
    }

    public static AuthResponse failure(String message) {
        return new AuthResponse(false, message, null, null, null);
    }
}
