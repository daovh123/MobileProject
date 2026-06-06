package com.mobileproject.mobileprojectbackend.payout;

/**
 * Trạng thái của yêu cầu rút tiền (payout) trong hệ thống.
 *
 * <ul>
 *   <li>{@link #PENDING} – Chờ SePay xử lý chuyển tiền ra</li>
 *   <li>{@link #PROCESSING} – Đang xử lý webhook từ SePay (claim nguyên tử)</li>
 *   <li>{@link #PAID} – Rút tiền thành công, đã ghi giao dịch EXPENSE</li>
 *   <li>{@link #FAILED} – Xử lý thất bại</li>
 * </ul>
 */
public enum PayoutRequestStatus {
    PENDING,
    PROCESSING,
    PAID,
    FAILED
}

