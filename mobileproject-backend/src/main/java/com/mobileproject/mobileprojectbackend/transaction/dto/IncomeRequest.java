package com.mobileproject.mobileprojectbackend.transaction.dto;

/**
 * DTO request cho endpoint nạp tiền {@code POST /api/v1/transactions/income}.
 *
 * @param coupleId   ID cặp đôi (bắt buộc)
 * @param amount     số tiền nạp, phải &gt; 0 (bắt buộc)
 * @param targetType đích đến: "WALLET" (ví chung) hoặc "GOAL" (mục tiêu tiết kiệm)
 * @param goalId     ID mục tiêu (bắt buộc khi targetType = "GOAL")
 * @param note       ghi chú thêm
 */
public record IncomeRequest(
        String coupleId,
        Long amount,
        String targetType,
        String goalId,
        String note
) {
}