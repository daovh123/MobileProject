package com.mobileproject.mobileprojectbackend.moment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request tạo kỷ niệm mới.
 *
 * @param coupleId    ID couple (bắt buộc)
 * @param title       tiêu đề (tối đa 120 ký tự)
 * @param base64Image ảnh mã hóa base64 (bắt buộc)
 */
public record MomentRequest(
        @NotBlank String coupleId,
        @Size(max = 120) String title,
        @NotBlank String base64Image) {
}
