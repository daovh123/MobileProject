package com.mobileproject.mobileprojectbackend.place.dto;

import java.util.List;

public record PlaceFeatureSummaryResponse(
        long totalPlaces,
        long totalFoodPlaces,
        long totalDrinkPlaces,
        long totalPinnedPlaces,
        long totalPlacesWithCoordinates,
        List<CountItem> topProvinces,
        List<CountItem> topDistricts,
        List<String> topTags
) {
    public record CountItem(String name, long count) {
    }
}