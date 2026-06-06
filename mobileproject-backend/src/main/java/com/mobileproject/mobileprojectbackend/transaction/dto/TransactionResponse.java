package com.mobileproject.mobileprojectbackend.transaction.dto;

import java.time.Instant;

/**
 * DTO response trả về kết quả xử lý giao dịch.
 *
 * @param success        {@code true} nếu giao dịch thành công
 * @param message        thông báo mô tả kết quả
 * @param transactionId  ID giao dịch đã lưu (null nếu thất bại)
 * @param amount         số tiền giao dịch
 * @param type           loại giao dịch ("INCOME" / "EXPENSE")
 * @param category       danh mục
 * @param note           ghi chú
 * @param currentBalance số dư hiện tại của ví chung sau giao dịch
 * @param createdAt      thời điểm tạo giao dịch
 */
public record TransactionResponse(
        boolean success,
        String message,
        String transactionId,
        Long amount,
        String type,
        String category,
        String note,
        Long currentBalance,  // Renamed from totalBalance for clarity in mobile context
        Instant createdAt
) {
    /**
     * Tạo response thành công.
     *
     * @param transactionId  ID giao dịch
     * @param amount         số tiền
     * @param type           loại giao dịch
     * @param category       danh mục
     * @param note           ghi chú
     * @param currentBalance số dư hiện tại
     * @return {@link TransactionResponse} thành công
     */
    public static TransactionResponse success(String transactionId, Long amount, String type,
                                                String category, String note, Long currentBalance) {
        return new TransactionResponse(true, "Transaction saved successfully", transactionId,
                amount, type, category, note, currentBalance, Instant.now());
    }

    /**
     * Tạo response thất bại.
     *
     * @param message thông báo lỗi
     * @return {@link TransactionResponse} thất bại
     */
    public static TransactionResponse failure(String message) {
        return new TransactionResponse(false, message, null, null, null, null, null, null, null);
    }
}