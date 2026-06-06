package com.mobileproject.mobileprojectbackend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO cho API đăng ký tài khoản mới.
 *
 * @param username tên đăng nhập (bắt buộc, 3-30 ký tự)
 * @param email    địa chỉ email (bắt buộc, đúng định dạng email)
 * @param password mật khẩu (bắt buộc, 6-100 ký tự)
 */
public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 30) String username,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, max = 100) String password
) {
}
