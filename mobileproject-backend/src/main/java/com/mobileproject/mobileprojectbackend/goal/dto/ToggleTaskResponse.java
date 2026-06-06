package com.mobileproject.mobileprojectbackend.goal.dto;

/**
 * DTO response trả về kết quả toggle task.
 *
 * @param success  {@code true} nếu toggle thành công
 * @param message  thông báo mô tả kết quả
 * @param goalId   ID mục tiêu
 * @param taskId   ID công việc đã toggle
 * @param progress tiến độ mới của mục tiêu (0.0 – 100.0)
 */
public record ToggleTaskResponse(
        boolean success,
        String message,
        String goalId,
        String taskId,
        Double progress
) {
    /**
     * Tạo response thành công.
     *
     * @param goalId   ID mục tiêu
     * @param taskId   ID công việc
     * @param progress tiến độ mới
     * @return {@link ToggleTaskResponse} thành công
     */
    public static ToggleTaskResponse success(String goalId, String taskId, Double progress) {
        return new ToggleTaskResponse(true, "Task toggled successfully", goalId, taskId, progress);
    }

    /**
     * Tạo response thất bại.
     *
     * @param message thông báo lỗi
     * @return {@link ToggleTaskResponse} thất bại
     */
    public static ToggleTaskResponse failure(String message) {
        return new ToggleTaskResponse(false, message, null, null, null);
    }
}
