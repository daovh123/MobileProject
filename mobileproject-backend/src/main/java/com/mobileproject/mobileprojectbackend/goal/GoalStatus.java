package com.mobileproject.mobileprojectbackend.goal;

/**
 * Trạng thái của mục tiêu trong hệ thống.
 *
 * <ul>
 *   <li>{@link #IN_PROGRESS} – Đang thực hiện</li>
 *   <li>{@link #ACHIEVED} – Đã đạt được mục tiêu</li>
 *   <li>{@link #FAILED} – Thất bại / hết hạn</li>
 * </ul>
 */
public enum GoalStatus {
    IN_PROGRESS,
    ACHIEVED,
    FAILED
}