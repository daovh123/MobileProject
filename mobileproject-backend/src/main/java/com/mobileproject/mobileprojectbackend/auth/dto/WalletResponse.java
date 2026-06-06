package com.mobileproject.mobileprojectbackend.auth.dto;

/**
 * Response DTO cho API thông tin ví chung của cặp đôi.
 *
 * @param idCouple     ID của cặp đôi
 * @param walletName   tên ví (mặc định "Ví chung")
 * @param totalBalance tổng số dư (đơn vị: VND)
 */
public record WalletResponse(
        String idCouple,
        String walletName,
        Long totalBalance
) {
    public WalletResponse(String idCouple, Long totalBalance) {
        this(idCouple, "Ví chung", totalBalance);
    }
}