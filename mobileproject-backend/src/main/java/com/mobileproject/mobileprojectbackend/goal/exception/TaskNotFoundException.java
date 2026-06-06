package com.mobileproject.mobileprojectbackend.goal.exception;

/**
 * Exception thrown khi không tìm thấy task theo ID trong mục tiêu tương lai.
 */
public class TaskNotFoundException extends RuntimeException {
    /**
     * Tạo exception với ID task không tìm thấy.
     *
     * @param taskId ID task
     */
    public TaskNotFoundException(String taskId) {
        super("Task not found with id: " + taskId);
    }

    /**
     * Tạo exception với message và cause.
     *
     * @param message thông báo lỗi
     * @param cause   nguyên nhân gốc
     */
    public TaskNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
