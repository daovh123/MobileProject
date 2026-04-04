package com.example.mobileproject.domain.entity

data class PlaceSearchPage(
    val items: List<Place> = emptyList(),
    val total: Long = 0,
    val page: Int = 0,
    val size: Int = 0,
)
