package com.mobileproject.mobileprojectbackend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfileUpsertRequest(
        @NotBlank @Size(max = 100) String fullName,
        @Size(max = 50) String nickName,
        @NotBlank String birthDate,
        @NotBlank @Size(max = 20) String gender
) {
}
