package com.mobileproject.mobileprojectbackend.transaction.dto;

public record IncomeRequest(
        String coupleId,
        Long amount,
        String targetType,
        String goalId,
        String note
) {
}