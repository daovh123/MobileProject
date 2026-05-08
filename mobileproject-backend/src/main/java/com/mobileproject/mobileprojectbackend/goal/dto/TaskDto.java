package com.mobileproject.mobileprojectbackend.goal.dto;

public record TaskDto(
        String taskId,
        String content,
        boolean isCompleted
) {
}
