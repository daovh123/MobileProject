package com.mobileproject.mobileprojectbackend.goal.dto;

import com.mobileproject.mobileprojectbackend.goal.GoalType;

import java.time.Instant;
import java.util.List;

public record CreateGoalRequest(
        String coupleId,
        String name,
        String category,
        GoalType type,
        Long targetAmount,
        Instant deadline,
        List<TaskDto> tasks
) {
}