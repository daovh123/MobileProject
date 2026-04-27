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
        Long currentBalance,  // Renamed from totalBalance for clarity in mobile context
        Instant createdAt
) {
    public static TransactionResponse success(String transactionId, Long amount, String type,
                                                String category, String note, Long currentBalance) {
        return new TransactionResponse(true, "Transaction saved successfully", transactionId,
                amount, type, category, note, currentBalance, Instant.now());
    }

    public static TransactionResponse failure(String message) {
        return new TransactionResponse(false, message, null, null, null, null, null, null, null);
    }
}