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
        String phoneNumber,
        boolean profileCompleted,
        boolean coupleConnected,
        String avatarUrl,
        String avatarFrameId
) {
    public static ProfileResponse success(String message,
                                          String username,
                                          String fullName,
                                          String nickName,
                                          String birthDate,
                                          String gender,
                                          String email,
                                          String phoneNumber,
                                          boolean profileCompleted,
                                          boolean coupleConnected,
                                          String avatarUrl,
                                          String avatarFrameId) {
        return new ProfileResponse(true, message, username, fullName, nickName, birthDate, gender, email, phoneNumber,
                profileCompleted, coupleConnected, avatarUrl, avatarFrameId);
    }
}
