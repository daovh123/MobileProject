package com.mobileproject.mobileprojectbackend.goal.dto;

import java.time.Instant;

public record ContributeResponse(
        boolean success,
        String message,
        String contributionId,
        String goalId,
        Long amount,
        Long currentAmount,
        Long walletBalance,
        Instant timestamp
) {
    public static ContributeResponse success(String contributionId, String goalId, Long amount,
                                              Long currentAmount, Long walletBalance) {
        return new ContributeResponse(true, "Contribution successful", contributionId, goalId,
                amount, currentAmount, walletBalance, Instant.now());
    }

    public static ContributeResponse failure(String message) {
        return new ContributeResponse(false, message, null, null, null, null, null, null);
    }
}