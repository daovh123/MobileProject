package com.example.mobileproject.data.model.map

data class LocationDto(
    val latitude: Double?,
    val longitude: Double?,
    val updatedAt: String?,
)

data class MapLastLocationsResponseDto(
    val success: Boolean,
    val message: String,
    val coupleId: String?,
    val myLocation: LocationDto?,
    val partnerLocation: LocationDto?,
)
