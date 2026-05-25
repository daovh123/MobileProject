package com.example.mobileproject.domain.repository

import com.example.mobileproject.domain.entity.Place
import com.example.mobileproject.domain.entity.ExplorePlan
import com.example.mobileproject.domain.entity.PlaceFilterOptions
import com.example.mobileproject.domain.entity.PlaceSearchPage

interface PlaceRepository {
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
    ): PlaceSearchPage

    suspend fun getRandomPlace(
        query: String?,
        province: String?,
        district: String?,
        type: String,
        minRating: Double?,
        nearLat: Double?,
        nearLng: Double?,
        radiusKm: Double?,
    ): Place

    suspend fun getExplorePlan(
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
    ): ExplorePlan

    suspend fun getFilterOptions(): PlaceFilterOptions

    suspend fun getVietnamProvinces(): List<String>
}
