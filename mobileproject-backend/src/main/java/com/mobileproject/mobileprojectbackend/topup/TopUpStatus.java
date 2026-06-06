package com.mobileproject.mobileprojectbackend.topup;

/**
 * Trạng thái tổng quát cho yêu cầu nạp tiền (legacy).
 *
 * <ul>
 *   <li>{@link #PENDING} – Chờ xử lý</li>
 *   <li>{@link #PAID} – Đã thanh toán</li>
 *   <li>{@link #FAILED} – Thất bại</li>
 *   <li>{@link #EXPIRED} – Hết hạn</li>
 * </ul>
 */
public enum TopUpStatus {
    PENDING,
    PAID,
    FAILED,
    EXPIRED
}
