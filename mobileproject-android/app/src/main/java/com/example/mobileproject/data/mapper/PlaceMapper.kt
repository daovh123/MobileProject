package com.example.mobileproject.data.mapper

import com.example.mobileproject.data.model.place.PlaceDto
import com.example.mobileproject.data.model.place.PlaceFilterOptionsDto
import com.example.mobileproject.data.model.place.VietnamProvinceDto
import com.example.mobileproject.domain.entity.PlaceFilterOptions
import com.example.mobileproject.domain.entity.Place
import java.util.Locale

fun PlaceDto.toDomain(): Place {
    return Place(
        id = id,
        name = name,
        address = address,
        district = district,
        province = province,
        effectiveTag = effectiveTag ?: category,
        category = category,
        mealType = mealType,
        rating = rating,
        reviewCount = reviewCount,
        openingHours = openingHours,
        priceRange = priceRange,
        imageUrl = imageUrl,
        googleMapsUrl = googleMapsUrl,
        lat = lat,
        lng = lng,
    )
}

fun PlaceFilterOptionsDto.toDomain(): PlaceFilterOptions {
    return PlaceFilterOptions(
        districts = districts,
        provinces = provinces,
    )
}

fun List<VietnamProvinceDto>.toProvinceNames(): List<String> {
    return asSequence()
        .mapNotNull { it.name?.trim() }
        .filter { it.isNotBlank() }
        .distinctBy { it.lowercase(Locale.ROOT) }
        .sortedBy { it.lowercase(Locale.ROOT) }
        .toList()
}