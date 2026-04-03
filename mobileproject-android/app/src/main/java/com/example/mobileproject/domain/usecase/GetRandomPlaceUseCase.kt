package com.example.mobileproject.domain.usecase

import com.example.mobileproject.domain.entity.Place
import com.example.mobileproject.domain.repository.PlaceRepository
import javax.inject.Inject

class GetRandomPlaceUseCase @Inject constructor(
    private val placeRepository: PlaceRepository,
) {
    suspend operator fun invoke(
        query: String?,
        province: String?,
        district: String?,
        type: String,
        minRating: Double?,
        nearLat: Double? = null,
        nearLng: Double? = null,
        radiusKm: Double? = null,
    ): Place {
        return placeRepository.getRandomPlace(
            query = query,
            province = province,
            district = district,
            type = type,
            minRating = minRating,
            nearLat = nearLat,
            nearLng = nearLng,
            radiusKm = radiusKm,
        )
    }
}