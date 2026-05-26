package com.mobileproject.mobileprojectbackend.topup.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateTopUpRequest(
        @NotBlank String coupleId,
        @NotNull @Positive Long amount,
        String bankId,
        String bankName,
        String note
) {
    public CreateTopUpRequest(String coupleId, Long amount) {
        this(coupleId, amount, null, null, null);
    }
}
