package com.example.mobileproject.data.datasource.remote

import com.example.mobileproject.data.model.place.ExplorePlanRequestDto
import com.example.mobileproject.data.model.place.ExplorePlanResponseDto
import com.example.mobileproject.data.model.place.PlaceDto
import com.example.mobileproject.data.model.place.PlaceFilterOptionsDto
import com.example.mobileproject.data.model.place.PlaceSearchResponseDto
import com.example.mobileproject.data.model.place.VietnamProvinceDto
import javax.inject.Inject

class PlaceRemoteDataSource @Inject constructor(
    private val placeApiService: PlaceApiService,
) {
    suspend fun searchPlaces(
        query: String?,
        province: String?,
        district: String?,
        type: String,
        minRating: Double?,
        nearLat: Double?,
        nearLng: Double?,
        radiusKm: Double?,
        page: Int,
        size: Int,
        sort: String,
    ): PlaceSearchResponseDto {
        return placeApiService.searchPlaces(
            query = query,
            province = province,
            district = district,
            type = type,
            minRating = minRating,
            nearLat = nearLat,
            nearLng = nearLng,
            radiusKm = radiusKm,
            page = page,
            size = size,
            sort = sort,
        )
    }

    suspend fun randomPlace(
        query: String?,
        province: String?,
        district: String?,
        type: String,
        minRating: Double?,
        nearLat: Double?,
        nearLng: Double?,
        radiusKm: Double?,
    ): PlaceDto {
        return placeApiService.randomPlace(
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

    suspend fun getExplorePlan(
        budget: Long,
        peopleCount: Int,
        desiredStops: Int,
        query: String?,
        province: String?,
        district: String?,
        type: String,
        minRating: Double?,
        nearLat: Double?,
        nearLng: Double?,
        radiusKm: Double?,
        excludePlaceIds: List<String>,
        viewedPlaceIds: List<String>,
        gonePlaceIds: List<String>,
        sentPlaceIds: List<String>,
        recentKeywords: List<String>,
    ): ExplorePlanResponseDto {
        return placeApiService.getExplorePlan(
            ExplorePlanRequestDto(
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
            ),
        )
    }

    suspend fun getFilterOptions(): PlaceFilterOptionsDto {
        return placeApiService.getFilterOptions()
    }

    suspend fun getVietnamProvinces(): List<VietnamProvinceDto> {
        return placeApiService.getVietnamProvinces(depth = 1)
    }
}
