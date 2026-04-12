package com.mobileproject.mobileprojectbackend.transaction.dto;

import java.time.Instant;

public record TransactionResponse(
        boolean success,
        String message,
        String transactionId,
        Long amount,
        String type,
        String category,
        String note,
        Long totalBalance,
        Instant createdAt
) {
    public static TransactionResponse success(String transactionId, Long amount, String type,
                                                String category, String note, Long totalBalance) {
        return new TransactionResponse(true, "Transaction saved successfully", transactionId,
                amount, type, category, note, totalBalance, Instant.now());
    }

    public static TransactionResponse failure(String message) {
        return new TransactionResponse(false, message, null, null, null, null, null, null, null);
    }
}