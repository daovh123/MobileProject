package com.example.mobileproject.domain.usecase

import com.example.mobileproject.domain.entity.ExplorePlan
import com.example.mobileproject.domain.repository.PlaceRepository
import javax.inject.Inject

class GetExplorePlanUseCase @Inject constructor(
    private val placeRepository: PlaceRepository,
) {
    suspend operator fun invoke(
        budget: Long,
        peopleCount: Int,
        desiredStops: Int,
        query: String? = null,
        province: String? = null,
        district: String? = null,
        type: String = "all",
        minRating: Double? = null,
        nearLat: Double? = null,
        nearLng: Double? = null,
        radiusKm: Double? = null,
        excludePlaceIds: List<String> = emptyList(),
        viewedPlaceIds: List<String> = emptyList(),
        gonePlaceIds: List<String> = emptyList(),
        sentPlaceIds: List<String> = emptyList(),
        recentKeywords: List<String> = emptyList(),
    ): ExplorePlan {
        return placeRepository.getExplorePlan(
            budget = budget,
            peopleCount = peopleCount,
            desiredStops = desiredStops,
            query = query,
            province = province,
            district = district,
            type = type,
            minRating = minRating,
            nearLat = nearLat,
            nearLng = nearLng,
            radiusKm = radiusKm,
            excludePlaceIds = excludePlaceIds,
            viewedPlaceIds = viewedPlaceIds,
            gonePlaceIds = gonePlaceIds,
            sentPlaceIds = sentPlaceIds,
            recentKeywords = recentKeywords,
        )
    }
}
