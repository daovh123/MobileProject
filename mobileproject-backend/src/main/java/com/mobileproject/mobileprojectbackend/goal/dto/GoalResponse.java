package com.mobileproject.mobileprojectbackend.goal.dto;

import com.mobileproject.mobileprojectbackend.goal.GoalStatus;
import com.mobileproject.mobileprojectbackend.goal.GoalType;

import java.time.Instant;
import java.util.List;

public record GoalResponse(
        boolean success,
        String message,
        String goalId,
        String name,
        String category,
        GoalType type,
        GoalStatus status,
        Instant deadline,
        Instant createdAt,
        Long targetAmount,
        Long currentAmount,
        Double progress,
        List<TaskDto> tasks
) {
    public static GoalResponse success(String goalId, String name, String category, GoalType type,
                                       GoalStatus status, Instant deadline, Instant createdAt,
                                       Long targetAmount, Long currentAmount,
                                       Double progress, List<TaskDto> tasks) {
        return new GoalResponse(true, "Operation successful", goalId, name, category,
                type, status, deadline, createdAt, targetAmount, currentAmount, progress, tasks);
    }

    public static GoalResponse failure(String message) {
        return new GoalResponse(false, message, null, null, null,
                null, null, null, null, null, null, null, null);
    }
}