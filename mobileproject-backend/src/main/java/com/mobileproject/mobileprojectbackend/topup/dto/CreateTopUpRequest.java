package com.mobileproject.mobileprojectbackend.topup.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO request để tạo yêu cầu nạp tiền mới.
 *
 * @param coupleId ID cặp đôi (bắt buộc, {@code @NotBlank})
 * @param amount   số tiền nạp VND (bắt buộc, {@code @NotNull @Positive})
 * @param bankId   mã ngân hàng tùy chọn (nếu trống dùng cấu hình mặc định)
 * @param bankName tên ngân hàng tùy chọn
 * @param note     ghi chú thêm
 */
public record CreateTopUpRequest(
        @NotBlank String coupleId,
        @NotNull @Positive Long amount,
        String bankId,
        String bankName,
        String note
) {
    /**
     * Constructor tiện lợi chỉ với coupleId và amount.
     *
     * @param coupleId ID cặp đôi
     * @param amount   số tiền nạp
     */
    public CreateTopUpRequest(String coupleId, Long amount) {
        this(coupleId, amount, null, null, null);
    }
}
