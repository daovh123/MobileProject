package com.mobileproject.mobileprojectbackend.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record CoupleRequestCreateRequest(
        @NotBlank String partnerCode
) {
}
