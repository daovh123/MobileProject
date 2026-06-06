package com.mobileproject.mobileprojectbackend.topup;

/**
 * Trạng thái của yêu cầu nạp tiền trong hệ thống.
 *
 * <ul>
 *   <li>{@link #PENDING} – Chờ người dùng chuyển khoản</li>
 *   <li>{@link #PROCESSING} – Đang xử lý webhook từ SePay (claim nguyên tử)</li>
 *   <li>{@link #PAID} – Nạp tiền thành công, đã cập nhật số dư</li>
 *   <li>{@link #FAILED} – Xử lý thất bại</li>
 * </ul>
 */
public enum TopUpRequestStatus {
    PENDING,
    PROCESSING,
    PAID,
    FAILED
}
