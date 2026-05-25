package com.example.mobileproject.data.repository

import com.example.mobileproject.data.datasource.remote.PlaceRemoteDataSource
import com.example.mobileproject.data.mapper.toDomain
import com.example.mobileproject.data.mapper.toProvinceNames
import com.example.mobileproject.domain.entity.ExplorePlan
import com.example.mobileproject.domain.entity.Place
import com.example.mobileproject.domain.entity.PlaceFilterOptions
import com.example.mobileproject.domain.entity.PlaceSearchPage
import com.example.mobileproject.domain.repository.PlaceRepository
import javax.inject.Inject

class PlaceRepositoryImpl @Inject constructor(
    private val placeRemoteDataSource: PlaceRemoteDataSource,
) : PlaceRepository {

    override suspend fun searchPlaces(
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
    ): PlaceSearchPage {
        val response = placeRemoteDataSource.searchPlaces(
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

        return PlaceSearchPage(
            items = response.items.map { it.toDomain() },
            total = response.total,
            page = response.page,
            size = response.size,
        )
    }

    override suspend fun getRandomPlace(
        query: String?,
        province: String?,
        district: String?,
        type: String,
        minRating: Double?,
        nearLat: Double?,
        nearLng: Double?,
        radiusKm: Double?,
    ): Place {
        return placeRemoteDataSource.randomPlace(
            query = query,
            province = province,
            district = district,
            type = type,
            minRating = minRating,
            nearLat = nearLat,
            nearLng = nearLng,
            radiusKm = radiusKm,
        ).toDomain()
    }

    override suspend fun getExplorePlan(
        randomSeed: Long?,
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
    ): ExplorePlan {
        return placeRemoteDataSource.getExplorePlan(
            randomSeed = randomSeed,
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
        ).toDomain()
    }

    override suspend fun getFilterOptions(): PlaceFilterOptions {
        return placeRemoteDataSource.getFilterOptions().toDomain()
    }

    override suspend fun getVietnamProvinces(): List<String> {
        return placeRemoteDataSource.getVietnamProvinces().toProvinceNames()
    }
}
