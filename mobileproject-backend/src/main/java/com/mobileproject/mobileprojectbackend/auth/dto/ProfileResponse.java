package com.mobileproject.mobileprojectbackend.auth.dto;

/**
 * Response DTO cho các API hồ sơ cá nhân.
 *
 * @param success          trạng thái thành công/thất bại
 * @param message          thông báo mô tả kết quả
 * @param username         tên đăng nhập
 * @param fullName         họ và tên đầy đủ
 * @param nickName         biệt danh
 * @param birthDate        ngày sinh (ISO-8601)
 * @param gender           giới tính (MALE/FEMALE/OTHER)
 * @param email            địa chỉ email
 * @param phoneNumber      số điện thoại
 * @param profileCompleted cờ hồ sơ đã hoàn thành
 * @param coupleConnected  cờ đã ghép đôi
 * @param avatarUrl        URL avatar (Base64 Data URL)
 * @param avatarFrameId    ID khung viền avatar
 */
public record ProfileResponse(
        boolean success,
        String message,
        String username,
        String fullName,
        String nickName,
        String birthDate,
        String gender,
        String email,
        String phoneNumber,
        boolean profileCompleted,
        boolean coupleConnected,
        String avatarUrl,
        String avatarFrameId
) {
    /**
     * Tạo ProfileResponse thành công.
     *
     * @param message          thông báo
     * @param username         tên đăng nhập
     * @param fullName         họ tên
     * @param nickName         biệt danh
     * @param birthDate        ngày sinh
     * @param gender           giới tính
     * @param email            email
     * @param phoneNumber      số điện thoại
     * @param profileCompleted trạng thái hồ sơ
     * @param coupleConnected  trạng thái ghép đôi
     * @param avatarUrl        URL avatar
     * @param avatarFrameId    ID khung avatar
     * @return ProfileResponse thành công
     */
    public static ProfileResponse success(String message,
                                          String username,
                                          String fullName,
                                          String nickName,
                                          String birthDate,
                                          String gender,
                                          String email,
                                          String phoneNumber,
                                          boolean profileCompleted,
                                          boolean coupleConnected,
                                          String avatarUrl,
                                          String avatarFrameId) {
        return new ProfileResponse(true, message, username, fullName, nickName, birthDate, gender, email, phoneNumber,
                profileCompleted, coupleConnected, avatarUrl, avatarFrameId);
    }
}
