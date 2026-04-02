package com.example.mobileproject.data.datasource.remote

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
        openNow: Boolean?,
        minRating: Double?,
        page: Int,
        size: Int,
        sort: String,
    ): PlaceSearchResponseDto {
        return placeApiService.searchPlaces(
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

    suspend fun randomPlace(
        query: String?,
        province: String?,
        district: String?,
        type: String,
        openNow: Boolean?,
        minRating: Double?,
    ): PlaceDto {
        return placeApiService.randomPlace(
            query = query,
            province = province,
            district = district,
            type = type,
            openNow = openNow,
            minRating = minRating,
        )
    }

    suspend fun getFilterOptions(): PlaceFilterOptionsDto {
        return placeApiService.getFilterOptions()
    }

    suspend fun getVietnamProvinces(): List<VietnamProvinceDto> {
        return placeApiService.getVietnamProvinces(depth = 1)
    }
}