package com.example.mobileproject.data.repository

import com.example.mobileproject.data.datasource.remote.PlaceRemoteDataSource
import com.example.mobileproject.data.mapper.toDomain
import com.example.mobileproject.data.mapper.toProvinceNames
import com.example.mobileproject.domain.entity.Place
import com.example.mobileproject.domain.entity.PlaceFilterOptions
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
    ): List<Place> {
        return placeRemoteDataSource
            .searchPlaces(
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
            .items
            .map { it.toDomain() }
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

    override suspend fun getFilterOptions(): PlaceFilterOptions {
        return placeRemoteDataSource.getFilterOptions().toDomain()
    }

    override suspend fun getVietnamProvinces(): List<String> {
        return placeRemoteDataSource.getVietnamProvinces().toProvinceNames()
    }
}