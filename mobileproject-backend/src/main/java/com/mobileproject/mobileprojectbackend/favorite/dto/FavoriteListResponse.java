package com.mobileproject.mobileprojectbackend.favorite.dto;

import com.mobileproject.mobileprojectbackend.place.dto.PlaceDto;

import java.util.List;

public record FavoriteListResponse(
        boolean success,
        String message,
        List<PlaceDto> places) {
}
