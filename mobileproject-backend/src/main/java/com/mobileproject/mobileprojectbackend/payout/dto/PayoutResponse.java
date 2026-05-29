package com.mobileproject.mobileprojectbackend.payout.dto;

import java.time.Instant;

public record PayoutResponse(
        boolean success,
        String message,
        String id,
        String coupleId,
        Long amount,
        String status,
        String transferCode,
        Long currentBalance,
        Instant createdAt,
        Instant paidAt
) {
    public static PayoutResponse failure(String message) {
        return new PayoutResponse(false, message, null, null, null, null, null, null, null, null);
    }
}

