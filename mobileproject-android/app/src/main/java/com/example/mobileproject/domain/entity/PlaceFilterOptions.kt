package com.example.mobileproject.domain.entity

data class PlaceFilterOptions(
    val districts: List<String> = emptyList(),
    val provinces: List<String> = emptyList(),
)