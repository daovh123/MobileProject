package com.mobileproject.mobileprojectbackend.auth.dto;

/**
 * Response DTO cho API upload avatar.
 *
 * @param success   trạng thái thành công/thất bại
 * @param message   thông báo mô tả kết quả
 * @param avatarUrl URL avatar dạng Base64 Data URL (data:image/...;base64,...)
 */
public record AvatarUploadResponse(boolean success, String message, String avatarUrl) {}
