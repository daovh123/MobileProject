package com.mobileproject.mobileprojectbackend.goal.dto;

import com.mobileproject.mobileprojectbackend.goal.GoalStatus;

import java.time.Instant;

public record GoalResponse(
        boolean success,
        String message,
        String goalId,
        String name,
        String category,
        Long targetAmount,
        Long currentAmount,
        GoalStatus status,
        Instant deadline,
        Instant createdAt
) {
    public static GoalResponse success(String goalId, String name, String category, Long targetAmount,
                                       Long currentAmount, GoalStatus status,
                                       Instant deadline, Instant createdAt) {
        return new GoalResponse(true, "Goal created successfully", goalId, name, category,
                targetAmount, currentAmount, status, deadline, createdAt);
    }

    public static GoalResponse failure(String message) {
        return new GoalResponse(false, message, null, null, null, null, null, null, null, null);
    }
}