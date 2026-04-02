package com.mobileproject.mobileprojectbackend.auth.dto;

public record CoupleCodeResponse(
        boolean success,
        String message,
        String myCode,
        String myCodeExpiresAt
) {
    public static CoupleCodeResponse success(String message, String myCode, String myCodeExpiresAt) {
        return new CoupleCodeResponse(true, message, myCode, myCodeExpiresAt);
    }
}
