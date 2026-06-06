package com.mobileproject.mobileprojectbackend.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO cho API gửi yêu cầu ghép đôi.
 *
 * @param partnerCode mã ghép đôi 6 số của đối tác (bắt buộc, hỗ trợ cả XXX-XXX và XXXXXX)
 */
public record CoupleRequestCreateRequest(
        @NotBlank String partnerCode
) {
}
