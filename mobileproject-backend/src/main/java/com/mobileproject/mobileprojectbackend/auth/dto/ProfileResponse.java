package com.mobileproject.mobileprojectbackend.auth.dto;

public record ProfileResponse(
        boolean success,
        String message,
        String username,
        String fullName,
        String nickName,
        String birthDate,
        String gender,
        String email,
        boolean profileCompleted,
        boolean coupleConnected
) {
    public static ProfileResponse success(String message,
                                          String username,
                                          String fullName,
                                          String nickName,
                                          String birthDate,
                                          String gender,
                                          String email,
                                          boolean profileCompleted,
                                          boolean coupleConnected) {
        return new ProfileResponse(true, message, username, fullName, nickName, birthDate, gender, email, profileCompleted, coupleConnected);
    }
}
