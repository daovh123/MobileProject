package com.mobileproject.mobileprojectbackend.map.dto;

/**
 * DTO vị trí địa lý của một user.
 *
 * @param latitude  vĩ độ
 * @param longitude kinh độ
 * @param updatedAt thời điểm cập nhật (ISO-8601)
 */
public record LocationDto(
        Double latitude,
        Double longitude,
        String updatedAt
) {
}
