package com.mobileproject.mobileprojectbackend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO cho API tạo mới/cập nhật hồ sơ cá nhân.
 *
 * @param fullName    họ và tên đầy đủ (bắt buộc, tối đa 100 ký tự)
 * @param nickName    biệt danh (tùy chọn, tối đa 50 ký tự)
 * @param birthDate   ngày sinh (bắt buộc, hỗ trợ: yyyy-MM-dd, dd/MM/yyyy, d/M/yyyy)
 * @param gender      giới tính (bắt buộc, tối đa 20 ký tự, hỗ trợ: male/female/other, nam/nữ/khác)
 * @param email       địa chỉ email (tùy chọn, đúng định dạng, tối đa 150 ký tự)
 * @param phoneNumber số điện thoại (tùy chọn, tối đa 20 ký tự)
 */
public record ProfileUpsertRequest(
        @NotBlank @Size(max = 100) String fullName,
        @Size(max = 50) String nickName,
        @NotBlank String birthDate,
        @NotBlank @Size(max = 20) String gender,
        @Email @Size(max = 150) String email,
        @Size(max = 20) String phoneNumber
) {
}
