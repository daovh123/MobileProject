package com.mobileproject.mobileprojectbackend.auth.dto;

/**
 * Response DTO cho API xem hồ sơ đối tác đã ghép đôi.
 *
 * @param success     trạng thái thành công/thất bại
 * @param message     thông báo mô tả kết quả
 * @param paired      đã ghép đôi hay chưa
 * @param username    tên đăng nhập đối tác
 * @param fullName    họ tên đầy đủ đối tác
 * @param nickName    biệt danh đối tác
 * @param avatarUrl   URL avatar đối tác
 * @param startAt     thời điểm bắt đầu mối quan hệ (ISO-8601)
 * @param daysTogether số ngày bên nhau
 * @param birthDate   ngày sinh đối tác
 * @param gender      giới tính đối tác
 * @param phoneNumber số điện thoại đối tác
 */
public record CouplePartnerProfileResponse(
        boolean success,
        String message,
        boolean paired,
        String username,
        String fullName,
        String nickName,
        String avatarUrl,
        String startAt,
        Long daysTogether,
        String birthDate,
        String gender,
        String phoneNumber) {
    public static CouplePartnerProfileResponse success(String message,
            boolean paired,
            String username,
            String fullName,
            String nickName,
            String avatarUrl,
            String startAt,
            Long daysTogether,
            String birthDate,
            String gender,
            String phoneNumber) {
        return new CouplePartnerProfileResponse(
                true,
                message,
                paired,
                username,
                fullName,
                nickName,
                avatarUrl,
                startAt,
                daysTogether,
                birthDate,
                gender,
                phoneNumber);
    }
}
