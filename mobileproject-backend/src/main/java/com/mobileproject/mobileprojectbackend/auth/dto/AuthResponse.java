package com.mobileproject.mobileprojectbackend.auth.dto;

public record AuthResponse(
        boolean success,
        String message,
        String token,
        String username,
        String email,
        boolean profileCompleted,
        boolean coupleConnected
) {
    public static AuthResponse success(String message,
                                       String token,
                                       String username,
                                       String email,
                                       boolean profileCompleted,
                                       boolean coupleConnected) {
        return new AuthResponse(true, message, token, username, email, profileCompleted, coupleConnected);
    }

    public static AuthResponse failure(String message) {
        return new AuthResponse(false, message, null, null, null, false, false);
    }
}
