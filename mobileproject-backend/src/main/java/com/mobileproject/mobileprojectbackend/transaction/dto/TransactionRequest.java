package com.mobileproject.mobileprojectbackend.transaction.dto;

import com.mobileproject.mobileprojectbackend.transaction.TransactionType;

public record TransactionRequest(
        String coupleId,
        Long amount,
        TransactionType type,
        String category,
        String note
) {
}