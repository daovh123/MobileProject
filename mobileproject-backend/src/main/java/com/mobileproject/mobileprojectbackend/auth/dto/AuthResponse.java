package com.mobileproject.mobileprojectbackend.auth.dto;

/**
 * Response DTO cho các API xác thực (đăng ký, đăng nhập).
 *
 * @param success         trạng thái thành công/thất bại
 * @param message         thông báo mô tả kết quả
 * @param token           JWT token (null nếu thất bại)
 * @param username        tên đăng nhập (null nếu thất bại)
 * @param email           địa chỉ email (null nếu thất bại)
 * @param profileCompleted cờ hồ sơ đã hoàn thành
 * @param coupleConnected  cờ đã ghép đôi
 */
public record AuthResponse(
        boolean success,
        String message,
        String token,
        String username,
        String email,
        boolean profileCompleted,
        boolean coupleConnected
) {
    /**
     * Tạo AuthResponse thành công.
     *
     * @param message         thông báo
     * @param token           JWT token
     * @param username        tên đăng nhập
     * @param email           email
     * @param profileCompleted trạng thái hồ sơ
     * @param coupleConnected  trạng thái ghép đôi
     * @return AuthResponse thành công
     */
    public static AuthResponse success(String message,
                                       String token,
                                       String username,
                                       String email,
                                       boolean profileCompleted,
                                       boolean coupleConnected) {
        return new AuthResponse(true, message, token, username, email, profileCompleted, coupleConnected);
    }

    /**
     * Tạo AuthResponse thất bại.
     *
     * @param message thông báo lỗi
     * @return AuthResponse thất bại
     */
    public static AuthResponse failure(String message) {
        return new AuthResponse(false, message, null, null, null, false, false);
    }
}
