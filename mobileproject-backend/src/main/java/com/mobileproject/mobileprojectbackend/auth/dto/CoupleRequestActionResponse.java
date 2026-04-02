package com.mobileproject.mobileprojectbackend.auth.dto;

public record CoupleRequestActionResponse(
        boolean success,
        String message,
        String requestId,
        String status,
        String requesterUsername,
        String recipientUsername
) {
    public static CoupleRequestActionResponse success(String message,
                                                      String requestId,
                                                      String status,
                                                      String requesterUsername,
                                                      String recipientUsername) {
        return new CoupleRequestActionResponse(
                true,
                message,
                requestId,
                status,
                requesterUsername,
                recipientUsername
        );
    }
}
