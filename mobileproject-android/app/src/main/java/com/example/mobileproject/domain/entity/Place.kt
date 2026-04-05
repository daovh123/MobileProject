package com.example.mobileproject.domain.entity

data class Place(
    val id: String,
    val name: String?,
    val address: String?,
    val district: String?,
    val province: String?,
    val effectiveTag: String?,
    val category: String?,
    val mealType: String?,
    val rating: Double?,
    val reviewCount: Int?,
    val openHours: String?,
    val priceRange: String?,
    val imageUrl: String?,
    val googleMapsUrl: String?,
    val lat: Double?,
    val lng: Double?,
)