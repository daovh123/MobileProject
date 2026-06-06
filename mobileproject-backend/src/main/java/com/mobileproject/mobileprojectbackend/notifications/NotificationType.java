package com.mobileproject.mobileprojectbackend.notifications;

/**
 * Enum định nghĩa các loại thông báo trong hệ thống.
 */
public enum NotificationType {
    /** Thông báo thanh toán (topup/payout). */
    PAYMENT,
    /** Thông báo giao dịch tài chính. */
    TRANSACTION,
    /** Mục tiêu tiết kiệm được tạo. */
    GOAL_CREATED,
    /** Mục tiêu tiết kiệm được cập nhật. */
    GOAL_UPDATED,
    /** Mục tiêu tiết kiệm hoàn thành. */
    GOAL_COMPLETED,
    /** Tin nhắn chat mới. */
    CHAT_MESSAGE,
    /** Partner chia sẻ kỷ niệm mới. */
    PARTNER_MEMORY,
    /** Nhắc nhở ngày đặc biệt (kỷ niệm, lễ). */
    SPECIAL_DAY
}
