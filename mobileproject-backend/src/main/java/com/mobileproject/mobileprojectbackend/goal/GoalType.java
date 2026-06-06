package com.mobileproject.mobileprojectbackend.goal;

/**
 * Loại mục tiêu trong hệ thống.
 *
 * <ul>
 *   <li>{@link #SAVING} – Mục tiêu tiết kiệm (có số tiền mục tiêu cụ thể)</li>
 *   <li>{@link #FUTURE} – Mục tiêu tương lai (dạng checklist công việc)</li>
 * </ul>
 */
public enum GoalType {
    SAVING,
    FUTURE
}
