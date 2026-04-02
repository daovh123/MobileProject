package com.mobileproject.mobileprojectbackend.place.dto;

public record PlaceDto(
        String id,
        String name,
        String address,
        String district,
        String category,
        String mealType,
        Double rating,
        Integer reviewCount,
        String openingHours,
        String priceRange,
        String imageUrl,
        Boolean pinned,
        String googleMapsUrl,
        Double lat,
        Double lng,
        String province,
        String normalizedDistrict,
        String effectiveTag,
        Boolean food,
        Boolean drink,
        String openTime,
        Boolean openNow,
        Double distanceKm
) {
}