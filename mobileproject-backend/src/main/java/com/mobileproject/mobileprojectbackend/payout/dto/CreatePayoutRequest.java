package com.mobileproject.mobileprojectbackend.payout.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePayoutRequest(
        @NotBlank String coupleId,
        @NotNull @Min(1) Long amount
) {
}

