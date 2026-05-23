package com.mobileproject.mobileprojectbackend.place.dto;

public record ExplorePlanItemResponse(
        int stopOrder,
        String experienceType,
        Long estimatedCost,
        String reason,
        PlaceDto place) {
}
