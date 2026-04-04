package com.example.mobileproject.data.model.favorite

import com.example.mobileproject.data.model.place.PlaceDto

data class FavoriteToggleResponseDto(
    val success: Boolean,
    val message: String,
    val favoriteId: String?,
    val placeId: String?,
    val createdAt: String?,
)

data class FavoriteListResponseDto(
    val success: Boolean,
    val message: String,
    val places: List<PlaceDto>,
)

data class HistoryListResponseDto(
    val success: Boolean,
    val message: String,
    val places: List<PlaceDto>,
)
