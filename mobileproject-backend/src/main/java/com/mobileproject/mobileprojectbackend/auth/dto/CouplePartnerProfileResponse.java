package com.mobileproject.mobileprojectbackend.auth.dto;

public record CouplePartnerProfileResponse(
        boolean success,
        String message,
        boolean paired,
        String username,
        String fullName,
        String nickName,
        String avatarUrl,
        String startAt,
        Long daysTogether) {
    public static CouplePartnerProfileResponse success(String message,
            boolean paired,
            String username,
            String fullName,
            String nickName,
            String avatarUrl,
            String startAt,
            Long daysTogether) {
        return new CouplePartnerProfileResponse(
                true,
                message,
                paired,
                username,
                fullName,
                nickName,
                avatarUrl,
                startAt,
                daysTogether);
    }
}
