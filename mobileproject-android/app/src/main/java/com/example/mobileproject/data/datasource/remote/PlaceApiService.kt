package com.example.mobileproject.data.datasource.remote

import com.example.mobileproject.data.model.place.PlaceDto
import com.example.mobileproject.data.model.place.PlaceFilterOptionsDto
import com.example.mobileproject.data.model.place.PlaceSearchResponseDto
import com.example.mobileproject.data.model.place.VietnamProvinceDto
import retrofit2.http.GET
import retrofit2.http.Query

interface PlaceApiService {

    @GET("api/places")
    suspend fun searchPlaces(
        @Query("q") query: String?,
        @Query("province") province: String?,
        @Query("district") district: String?,
        @Query("type") type: String,
        @Query("openNow") openNow: Boolean?,
        @Query("minRating") minRating: Double?,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sort") sort: String,
    ): PlaceSearchResponseDto

    @GET("api/places/random")
    suspend fun randomPlace(
        @Query("q") query: String?,
        @Query("province") province: String?,
        @Query("district") district: String?,
        @Query("type") type: String,
        @Query("openNow") openNow: Boolean?,
        @Query("minRating") minRating: Double?,
    ): PlaceDto

    @GET("api/places/filter-options")
    suspend fun getFilterOptions(): PlaceFilterOptionsDto

    @GET("https://provinces.open-api.vn/api/v2/p/")
    suspend fun getVietnamProvinces(
        @Query("depth") depth: Int = 1,
    ): List<VietnamProvinceDto>
}