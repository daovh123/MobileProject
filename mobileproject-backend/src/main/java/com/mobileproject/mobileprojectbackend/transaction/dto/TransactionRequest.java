package com.mobileproject.mobileprojectbackend.transaction.dto;

import com.mobileproject.mobileprojectbackend.transaction.TransactionType;

/**
 * DTO request cho endpoint tạo giao dịch {@code POST /api/v1/transactions}.
 *
 * @param coupleId ID cặp đôi (bắt buộc)
 * @param amount   số tiền, phải &gt; 0 (bắt buộc)
 * @param type     loại giao dịch {@link TransactionType} (bắt buộc)
 * @param category danh mục giao dịch
 * @param note     ghi chú thêm
 */
public record TransactionRequest(
        String coupleId,
        Long amount,
        TransactionType type,
        String category,
        String note
) {
}