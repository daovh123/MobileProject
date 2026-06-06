package com.mobileproject.mobileprojectbackend.goal.dto;

import java.time.Instant;

/**
 * DTO response trả về kết quả đóng góp vào mục tiêu.
 *
 * @param success        {@code true} nếu đóng góp thành công
 * @param message        thông báo mô tả kết quả
 * @param contributionId ID bản ghi đóng góp
 * @param goalId         ID mục tiêu
 * @param amount         số tiền đã đóng góp (VND)
 * @param currentAmount  tổng số tiền đã tiết kiệm trong mục tiêu
 * @param walletBalance  số dư ví chung hiện tại (null nếu đóng góp trực tiếp)
 * @param timestamp      thời điểm đóng góp
 */
public record ContributeResponse(
        boolean success,
        String message,
        String contributionId,
        String goalId,
        Long amount,
        Long currentAmount,
        Long walletBalance,
        Instant timestamp
) {
    /**
     * Tạo response thành công.
     *
     * @param contributionId ID đóng góp
     * @param goalId         ID mục tiêu
     * @param amount         số tiền
     * @param currentAmount  tổng tiền đã tiết kiệm
     * @param walletBalance  số dư ví
     * @return {@link ContributeResponse} thành công
     */
    public static ContributeResponse success(String contributionId, String goalId, Long amount,
                                              Long currentAmount, Long walletBalance) {
        return new ContributeResponse(true, "Contribution successful", contributionId, goalId,
                amount, currentAmount, walletBalance, Instant.now());
    }

    /**
     * Tạo response thất bại.
     *
     * @param message thông báo lỗi
     * @return {@link ContributeResponse} thất bại
     */
    public static ContributeResponse failure(String message) {
        return new ContributeResponse(false, message, null, null, null, null, null, null);
    }
}