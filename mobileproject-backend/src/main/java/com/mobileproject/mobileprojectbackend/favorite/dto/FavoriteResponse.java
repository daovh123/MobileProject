package com.mobileproject.mobileprojectbackend.favorite.dto;

import java.time.Instant;

public record FavoriteResponse(
        boolean success,
        String message,
        String favoriteId,
        String placeId,
        Instant createdAt) {
}
