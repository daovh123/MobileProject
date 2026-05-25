package com.mobileproject.mobileprojectbackend.goal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TaskDto(
        String taskId,
        String content,
        @JsonProperty("completed")
        boolean isCompleted
) {
}
