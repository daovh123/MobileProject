package com.mobileproject.mobileprojectbackend.goal.exception;

/**
 * Exception thrown khi không tìm thấy mục tiêu theo ID.
 * Thường được sử dụng trong service layer hoặc global exception handler.
 */
public class GoalNotFoundException extends RuntimeException {
    /**
     * Tạo exception với ID mục tiêu không tìm thấy.
     *
     * @param goalId ID mục tiêu
     */
    public GoalNotFoundException(String goalId) {
        super("Goal not found with id: " + goalId);
    }

    /**
     * Tạo exception với message và cause.
     *
     * @param message thông báo lỗi
     * @param cause   nguyên nhân gốc
     */
    public GoalNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
