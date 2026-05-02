package com.mobileproject.mobileprojectbackend.goal.dto;

import java.time.Instant;

public record CreateGoalRequest(
        String coupleId,
        String name,
        String category,
        Long targetAmount,
        Instant deadline
) {
}