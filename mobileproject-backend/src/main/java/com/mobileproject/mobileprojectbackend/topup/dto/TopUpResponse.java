package com.mobileproject.mobileprojectbackend.topup.dto;

import java.time.Instant;

public record TopUpResponse(
        boolean success,
        String message,
        String id,
        String coupleId,
        Long amount,
        String status,
        String transferCode,
        String bankId,
        String bankName,
        String accountNumber,
        String accountName,
        String transferContent,
        String qrContent,
        String qrImageUrl,
        Long currentBalance,
        Instant createdAt,
        Instant paidAt
) {
    public static TopUpResponse failure(String message) {
        return new TopUpResponse(false, message, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null);
    }

    public String topUpRequestId() {
        return id;
    }

    public String bankCode() {
        return bankId;
    }

    public String qrUrl() {
        return qrImageUrl;
    }
}
