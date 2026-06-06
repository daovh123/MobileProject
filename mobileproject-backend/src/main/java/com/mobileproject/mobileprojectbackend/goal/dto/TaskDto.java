package com.mobileproject.mobileprojectbackend.goal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO đại diện cho một công việc (task) trong mục tiêu tương lai.
 *
 * @param taskId      ID duy nhất của task (UUID, có thể null khi tạo mới)
 * @param content     nội dung mô tả công việc
 * @param isCompleted trạng thái hoàn thành, serialize là {@code "completed"} trong JSON
 */
public record TaskDto(
        String taskId,
        String content,
        @JsonProperty("completed")
        boolean isCompleted
) {
}
