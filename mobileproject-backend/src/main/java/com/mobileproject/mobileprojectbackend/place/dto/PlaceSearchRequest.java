package com.mobileproject.mobileprojectbackend.place.dto;

public record PlaceSearchRequest(
        String q,
        String province,
        String district,
        String type,
        Double minRating,
        Boolean openNow,
        Double nearLat,
        Double nearLng,
        Double radiusKm,
        String sort,
        int page,
        int size
) {
}