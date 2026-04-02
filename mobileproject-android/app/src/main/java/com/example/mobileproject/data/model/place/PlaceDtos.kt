package com.example.mobileproject.data.model.place

import com.google.gson.annotations.SerializedName

data class PlaceSearchResponseDto(
    @SerializedName("items")
    val items: List<PlaceDto> = emptyList(),
    @SerializedName("total")
    val total: Long = 0,
    @SerializedName("page")
    val page: Int = 0,
    @SerializedName("size")
    val size: Int = 0,
)

data class PlaceDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String?,
    @SerializedName("address")
    val address: String?,
    @SerializedName("district")
    val district: String?,
    @SerializedName("province")
    val province: String?,
    @SerializedName("effectiveTag")
    val effectiveTag: String?,
    @SerializedName("category")
    val category: String?,
    @SerializedName("mealType")
    val mealType: String?,
    @SerializedName("rating")
    val rating: Double?,
    @SerializedName("reviewCount")
    val reviewCount: Int?,
    @SerializedName("openingHours")
    val openingHours: String?,
    @SerializedName("priceRange")
    val priceRange: String?,
    @SerializedName("imageUrl")
    val imageUrl: String?,
    @SerializedName("googleMapsUrl")
    val googleMapsUrl: String?,
    @SerializedName("lat")
    val lat: Double?,
    @SerializedName("lng")
    val lng: Double?,
)

data class PlaceFilterOptionsDto(
    @SerializedName("districts")
    val districts: List<String> = emptyList(),
    @SerializedName("provinces")
    val provinces: List<String> = emptyList(),
)

data class VietnamProvinceDto(
    @SerializedName("code")
    val code: Int,
    @SerializedName("name")
    val name: String?,
)