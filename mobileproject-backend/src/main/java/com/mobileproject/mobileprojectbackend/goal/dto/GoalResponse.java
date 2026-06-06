package com.mobileproject.mobileprojectbackend.goal.dto;

import com.mobileproject.mobileprojectbackend.goal.GoalStatus;
import com.mobileproject.mobileprojectbackend.goal.GoalType;

import java.time.Instant;
import java.util.List;

/**
 * DTO response trả về kết quả tạo/truy vấn mục tiêu.
 *
 * @param success       {@code true} nếu thao tác thành công
 * @param message       thông báo mô tả kết quả
 * @param goalId        ID mục tiêu
 * @param name          tên mục tiêu
 * @param category      danh mục
 * @param type          loại mục tiêu (SAVING / FUTURE)
 * @param status        trạng thái (IN_PROGRESS / ACHIEVED / FAILED)
 * @param deadline      thời hạn
 * @param createdAt     thời điểm tạo
 * @param targetAmount  số tiền mục tiêu (chỉ cho SAVING)
 * @param currentAmount số tiền đã tiết kiệm (chỉ cho SAVING)
 * @param progress      tiến độ % (chỉ cho FUTURE)
 * @param tasks         danh sách tasks (chỉ cho FUTURE)
 */
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
    /**
     * Tạo response thành công.
     *
     * @param goalId        ID mục tiêu
     * @param name          tên mục tiêu
     * @param category      danh mục
     * @param type          loại mục tiêu
     * @param status        trạng thái
     * @param deadline      thời hạn
     * @param createdAt     thời điểm tạo
     * @param targetAmount  số tiền mục tiêu
     * @param currentAmount số tiền đã tiết kiệm
     * @param progress      tiến độ %
     * @param tasks         danh sách tasks
     * @return {@link GoalResponse} thành công
     */
    public static GoalResponse success(String goalId, String name, String category, GoalType type,
                                       GoalStatus status, Instant deadline, Instant createdAt,
                                       Long targetAmount, Long currentAmount,
                                       Double progress, List<TaskDto> tasks) {
        return new GoalResponse(true, "Operation successful", goalId, name, category,
                type, status, deadline, createdAt, targetAmount, currentAmount, progress, tasks);
    }

    /**
     * Tạo response thất bại.
     *
     * @param message thông báo lỗi
     * @return {@link GoalResponse} thất bại
     */
    public static GoalResponse failure(String message) {
        return new GoalResponse(false, message, null, null, null,
                null, null, null, null, null, null, null, null);
    }
}