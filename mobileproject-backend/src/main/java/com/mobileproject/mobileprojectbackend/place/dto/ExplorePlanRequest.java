package com.mobileproject.mobileprojectbackend.place.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ExplorePlanRequest(
        Long randomSeed,
        String q,
        String province,
        String district,
        String type,
        Double minRating,
        Double nearLat,
        Double nearLng,
        Double radiusKm,
        @NotNull @Min(1_000) Long budget,
        @Min(1) @Max(20) Integer peopleCount,
        @Min(1) @Max(5) Integer desiredStops,
        List<String> excludePlaceIds,
        List<String> viewedPlaceIds,
        List<String> gonePlaceIds,
        List<String> sentPlaceIds,
        List<String> recentKeywords) {
}
