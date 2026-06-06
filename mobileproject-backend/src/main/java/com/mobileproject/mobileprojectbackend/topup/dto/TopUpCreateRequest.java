package com.mobileproject.mobileprojectbackend.topup.dto;

/**
 * DTO request thay thế (legacy) để tạo yêu cầu nạp tiền.
 *
 * @param coupleId ID cặp đôi
 * @param amount   số tiền nạp VND
 * @param bankId   mã ngân hàng tùy chọn
 * @param bankName tên ngân hàng tùy chọn
 * @param note     ghi chú
 */
public record TopUpCreateRequest(
        String coupleId,
        Long amount,
        String bankId,
        String bankName,
        String note
) {
}
