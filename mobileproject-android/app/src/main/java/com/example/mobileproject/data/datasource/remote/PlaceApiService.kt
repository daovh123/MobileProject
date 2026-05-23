package com.example.mobileproject.data.datasource.remote

import com.example.mobileproject.data.model.place.ExplorePlanRequestDto
import com.example.mobileproject.data.model.place.ExplorePlanResponseDto
import com.example.mobileproject.data.model.place.PlaceDto
import com.example.mobileproject.data.model.place.PlaceFilterOptionsDto
import com.example.mobileproject.data.model.place.PlaceSearchResponseDto
import com.example.mobileproject.data.model.place.VietnamProvinceDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface PlaceApiService {

    @GET("api/places")
    suspend fun searchPlaces(
        @Query("q") query: String?,
        @Query("province") province: String?,
        @Query("district") district: String?,
        @Query("type") type: String,
        @Query("minRating") minRating: Double?,
        @Query("nearLat") nearLat: Double?,
        @Query("nearLng") nearLng: Double?,
        @Query("radiusKm") radiusKm: Double?,
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
        @Query("minRating") minRating: Double?,
        @Query("nearLat") nearLat: Double?,
        @Query("nearLng") nearLng: Double?,
        @Query("radiusKm") radiusKm: Double?,
    ): PlaceDto

    @POST("api/places/explore-plan")
    suspend fun getExplorePlan(
        @Body request: ExplorePlanRequestDto,
    ): ExplorePlanResponseDto

    @GET("api/places/filter-options")
    suspend fun getFilterOptions(): PlaceFilterOptionsDto

    @GET("https://provinces.open-api.vn/api/v2/p/")
    suspend fun getVietnamProvinces(
        @Query("depth") depth: Int = 1,
    ): List<VietnamProvinceDto>
}
