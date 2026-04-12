package com.mobileproject.mobileprojectbackend.goal.dto;

import java.time.Instant;

public record CreateGoalRequest(
        String coupleId,
        String name,
        Long targetAmount,
        Instant deadline
) {
}