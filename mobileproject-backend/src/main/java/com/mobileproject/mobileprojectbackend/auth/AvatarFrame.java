package com.mobileproject.mobileprojectbackend.auth;

/**
 * Record đại diện cho một mẫu khung viền avatar trong catalog.
 *
 * @param id          ID duy nhất của khung (ví dụ: "frame_rose")
 * @param name        Tên hiển thị (ví dụ: "Rose")
 * @param resourceKey Khóa tài nguyên để ánh xạ tới resource trên client (ví dụ: "rose")
 * @param color       Mã màu HEX của khung (ví dụ: "#E91E63")
 */
public record AvatarFrame(String id, String name, String resourceKey, String color) {}
