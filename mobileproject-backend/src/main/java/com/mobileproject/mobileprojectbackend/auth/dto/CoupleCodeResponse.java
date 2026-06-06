package com.mobileproject.mobileprojectbackend.auth.dto;

/**
 * Response DTO cho API tạo mã ghép đôi.
 *
 * @param success         trạng thái thành công/thất bại
 * @param message         thông báo mô tả kết quả
 * @param myCode          mã ghép đôi 6 số (định dạng XXX-XXX)
 * @param myCodeExpiresAt thời điểm hết hạn mã (ISO-8601)
 */
public record CoupleCodeResponse(
        boolean success,
        String message,
        String myCode,
        String myCodeExpiresAt
) {
    public static CoupleCodeResponse success(String message, String myCode, String myCodeExpiresAt) {
        return new CoupleCodeResponse(true, message, myCode, myCodeExpiresAt);
    }
}
