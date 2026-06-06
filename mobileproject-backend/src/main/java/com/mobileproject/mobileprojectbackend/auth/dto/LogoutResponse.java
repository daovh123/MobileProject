package com.mobileproject.mobileprojectbackend.auth.dto;

/**
 * Response DTO cho API đăng xuất.
 *
 * @param success trạng thái thành công/thất bại
 * @param message thông báo mô tả kết quả
 */
public record LogoutResponse(
        boolean success,
        String message
) {
    /**
     * Tạo LogoutResponse thành công.
     *
     * @param message thông báo
     * @return LogoutResponse thành công
     */
    public static LogoutResponse success(String message) {
        return new LogoutResponse(true, message);
    }

    /**
     * Tạo LogoutResponse thất bại.
     *
     * @param message thông báo lỗi
     * @return LogoutResponse thất bại
     */
    public static LogoutResponse failure(String message) {
        return new LogoutResponse(false, message);
    }
}