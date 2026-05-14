package com.mobileproject.mobileprojectbackend.moment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MomentRequest(
        @NotBlank String coupleId,
        @Size(max = 120) String title,
        @NotBlank String base64Image) {
}
