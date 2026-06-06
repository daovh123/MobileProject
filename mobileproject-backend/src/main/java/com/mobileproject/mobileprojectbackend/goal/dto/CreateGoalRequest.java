package com.mobileproject.mobileprojectbackend.goal.dto;

import com.mobileproject.mobileprojectbackend.goal.GoalType;

import java.time.Instant;
import java.util.List;

/**
 * DTO request để tạo mục tiêu mới.
 *
 * @param coupleId     ID cặp đôi (bắt buộc)
 * @param name         tên mục tiêu (bắt buộc)
 * @param category     danh mục mục tiêu
 * @param type         loại mục tiêu: SAVING hoặc FUTURE (bắt buộc)
 * @param targetAmount số tiền mục tiêu (bắt buộc cho SAVING)
 * @param deadline     thời hạn hoàn thành
 * @param tasks        danh sách tasks ban đầu (chỉ dùng cho FUTURE)
 */
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