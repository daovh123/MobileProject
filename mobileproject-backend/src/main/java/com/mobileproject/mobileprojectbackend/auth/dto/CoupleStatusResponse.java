package com.mobileproject.mobileprojectbackend.auth.dto;

public record CoupleStatusResponse(
        boolean success,
        String message,
        boolean profileCompleted,
        boolean paired,
        String partnerUsername,
        String myCoupleCode,
        String myCoupleCodeExpiresAt,
        String incomingRequestId,
        String incomingRequesterUsername,
        String incomingRequesterDisplayName,
        String incomingCreatedAt,
        String outgoingRequestId,
        String outgoingRecipientUsername,
        String outgoingStatus,
        String outgoingUpdatedAt
) {
    public static CoupleStatusResponse success(String message,
                                               boolean profileCompleted,
                                               boolean paired,
                                               String partnerUsername,
                                               String myCoupleCode,
                                               String myCoupleCodeExpiresAt,
                                               String incomingRequestId,
                                               String incomingRequesterUsername,
                                               String incomingRequesterDisplayName,
                                               String incomingCreatedAt,
                                               String outgoingRequestId,
                                               String outgoingRecipientUsername,
                                               String outgoingStatus,
                                               String outgoingUpdatedAt) {
        return new CoupleStatusResponse(
                true,
                message,
                profileCompleted,
                paired,
                partnerUsername,
                myCoupleCode,
                myCoupleCodeExpiresAt,
                incomingRequestId,
                incomingRequesterUsername,
                incomingRequesterDisplayName,
                incomingCreatedAt,
                outgoingRequestId,
                outgoingRecipientUsername,
                outgoingStatus,
                outgoingUpdatedAt
        );
    }
}
