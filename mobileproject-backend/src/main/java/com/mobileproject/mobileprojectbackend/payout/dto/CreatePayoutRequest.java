package com.mobileproject.mobileprojectbackend.payout.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO request để tạo yêu cầu rút tiền.
 *
 * @param coupleId ID cặp đôi (bắt buộc, {@code @NotBlank})
 * @param amount   số tiền rút VND (bắt buộc, {@code @NotNull @Min(1)})
 */
public record CreatePayoutRequest(
        @NotBlank String coupleId,
        @NotNull @Min(1) Long amount
) {
}

