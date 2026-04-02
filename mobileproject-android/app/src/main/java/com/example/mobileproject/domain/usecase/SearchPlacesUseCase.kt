package com.example.mobileproject.domain.usecase

import com.example.mobileproject.domain.entity.Place
import com.example.mobileproject.domain.repository.PlaceRepository
import javax.inject.Inject

class SearchPlacesUseCase @Inject constructor(
    private val placeRepository: PlaceRepository,
) {
    suspend operator fun invoke(
        query: String?,
        province: String? = null,
        district: String? = null,
        type: String = "all",
        openNow: Boolean? = null,
        minRating: Double? = null,
        page: Int = 0,
        size: Int = 20,
        sort: String = "trending",
    ): List<Place> {
        return placeRepository.searchPlaces(
            query = query,
            province = province,
            district = district,
            type = type,
            openNow = openNow,
            minRating = minRating,
            page = page,
            size = size,
            sort = sort,
        )
    }
}