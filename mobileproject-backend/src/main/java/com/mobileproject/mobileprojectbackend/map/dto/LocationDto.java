package com.mobileproject.mobileprojectbackend.map.dto;

public record LocationDto(
        Double latitude,
        Double longitude,
        String updatedAt
) {
}
