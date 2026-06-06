package com.mobileproject.mobileprojectbackend.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO cho API đăng nhập.
 *
 * @param usernameOrEmail tên đăng nhập hoặc địa chỉ email (bắt buộc)
 * @param password        mật khẩu (bắt buộc)
 */
public record LoginRequest(
        @NotBlank String usernameOrEmail,
        @NotBlank String password
) {
}
