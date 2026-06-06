package com.mobileproject.mobileprojectbackend.auth.dto;

/**
 * Response DTO cho thông tin một mẫu khung viền avatar.
 *
 * @param id          ID duy nhất của khung (ví dụ: "frame_rose")
 * @param name        tên hiển thị (ví dụ: "Rose")
 * @param resourceKey khóa tài nguyên để ánh xạ resource trên client
 * @param color       mã màu HEX (ví dụ: "#E91E63")
 */
public record AvatarFrameResponse(String id, String name, String resourceKey, String color) {}
