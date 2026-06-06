package com.mobileproject.mobileprojectbackend.auth.dto;

/**
 * Request DTO cho API chọn khung viền avatar.
 *
 * @param frameId ID khung viền muốn chọn (null/rỗng để xóa khung hiện tại)
 */
public record AvatarFrameRequest(String frameId) {}
