package com.mobileproject.mobileprojectbackend.place.dto;

import java.util.List;

public record PlaceSearchResponse(
        List<PlaceDto> items,
        long total,
        int page,
        int size
) {
}