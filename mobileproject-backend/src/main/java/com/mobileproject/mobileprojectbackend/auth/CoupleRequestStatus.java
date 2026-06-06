package com.mobileproject.mobileprojectbackend.auth;

/**
 * Enum biểu diễn trạng thái của yêu cầu ghép đôi.
 *
 * <p>Luồng chuyển trạng thái:</p>
 * <ul>
 *   <li>{@link #PENDING} → {@link #ACCEPTED} (người nhận chấp nhận)</li>
 *   <li>{@link #PENDING} → {@link #REJECTED} (người nhận từ chối)</li>
 * </ul>
 */
public enum CoupleRequestStatus {
    /** Yêu cầu đang chờ xử lý. */
    PENDING,
    /** Yêu cầu đã được chấp nhận - hai người dùng đã được liên kết. */
    ACCEPTED,
    /** Yêu cầu đã bị từ chối. */
    REJECTED
}
