package com.mobileproject.mobileprojectbackend.goal.dto;

import com.mobileproject.mobileprojectbackend.goal.GoalStatus;

import java.time.Instant;

public record ContributeResponse(
        boolean success,
        String message,
        String contributionId,
        String goalId,
        Long amount,
        Long currentAmount,
        Long walletBalance,
        GoalStatus goalStatus,
        Long withdrawnAmount,
        Instant timestamp
) {
    public static ContributeResponse success(
            String contributionId,
            String goalId,
            Long amount,
            Long currentAmount,
            Long walletBalance,
            GoalStatus goalStatus,
            Long withdrawnAmount,
            String message
    ) {
        return new ContributeResponse(
                true,
                message,
                contributionId,
                goalId,
                amount,
                currentAmount,
                walletBalance,
                goalStatus,
                withdrawnAmount,
                Instant.now()
        );
    }

    public static ContributeResponse failure(String message) {
        return new ContributeResponse(false, message, null, null, null, null, null, null, null, null);
    }
}
