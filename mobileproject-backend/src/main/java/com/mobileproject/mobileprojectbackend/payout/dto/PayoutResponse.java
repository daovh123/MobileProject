package com.mobileproject.mobileprojectbackend.payout.dto;

import java.time.Instant;

/**
 * DTO response trả về kết quả tạo/truy vấn yêu cầu rút tiền.
 *
 * @param success        {@code true} nếu thao tác thành công
 * @param message        thông báo mô tả kết quả
 * @param id             ID yêu cầu rút tiền
 * @param coupleId       ID cặp đôi
 * @param amount         số tiền rút (VND)
 * @param status         trạng thái ("PENDING", "PROCESSING", "PAID", "FAILED")
 * @param transferCode   mã chuyển khoản
 * @param currentBalance số dư ví hiện tại sau khi tạo yêu cầu
 * @param createdAt      thời điểm tạo yêu cầu
 * @param paidAt         thời điểm rút tiền thành công (null nếu chưa)
 */
public record PayoutResponse(
        boolean success,
        String message,
        String id,
        String coupleId,
        Long amount,
        String status,
        String transferCode,
        Long currentBalance,
        Instant createdAt,
        Instant paidAt
) {
    /**
     * Tạo response thất bại.
     *
     * @param message thông báo lỗi
     * @return {@link PayoutResponse} thất bại với tất cả trường null
     */
    public static PayoutResponse failure(String message) {
        return new PayoutResponse(false, message, null, null, null, null, null, null, null, null);
    }
}

