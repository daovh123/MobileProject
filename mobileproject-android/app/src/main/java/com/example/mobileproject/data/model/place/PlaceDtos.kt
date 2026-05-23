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
    @SerializedName("openHours")
    val openHours: String?,
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

data class ExplorePlanRequestDto(
    @SerializedName("budget")
    val budget: Long,
    @SerializedName("peopleCount")
    val peopleCount: Int,
    @SerializedName("desiredStops")
    val desiredStops: Int,
    @SerializedName("q")
    val query: String?,
    @SerializedName("province")
    val province: String?,
    @SerializedName("district")
    val district: String?,
    @SerializedName("type")
    val type: String,
    @SerializedName("minRating")
    val minRating: Double?,
    @SerializedName("nearLat")
    val nearLat: Double?,
    @SerializedName("nearLng")
    val nearLng: Double?,
    @SerializedName("radiusKm")
    val radiusKm: Double?,
    @SerializedName("excludePlaceIds")
    val excludePlaceIds: List<String> = emptyList(),
    @SerializedName("viewedPlaceIds")
    val viewedPlaceIds: List<String> = emptyList(),
    @SerializedName("gonePlaceIds")
    val gonePlaceIds: List<String> = emptyList(),
    @SerializedName("sentPlaceIds")
    val sentPlaceIds: List<String> = emptyList(),
    @SerializedName("recentKeywords")
    val recentKeywords: List<String> = emptyList(),
)

data class ExplorePlanResponseDto(
    @SerializedName("summary")
    val summary: ExplorePlanSummaryDto,
    @SerializedName("items")
    val items: List<ExplorePlanItemDto> = emptyList(),
)

data class ExplorePlanSummaryDto(
    @SerializedName("totalBudget")
    val totalBudget: Long = 0L,
    @SerializedName("peopleCount")
    val peopleCount: Int = 0,
    @SerializedName("desiredStops")
    val desiredStops: Int = 0,
    @SerializedName("estimatedTotalCost")
    val estimatedTotalCost: Long = 0L,
    @SerializedName("lowBalance")
    val lowBalance: Boolean = false,
    @SerializedName("balanceMessage")
    val balanceMessage: String? = null,
    @SerializedName("suggestedDefaultBudget")
    val suggestedDefaultBudget: Long = 0L,
)

data class ExplorePlanItemDto(
    @SerializedName("stopOrder")
    val stopOrder: Int,
    @SerializedName("experienceType")
    val experienceType: String,
    @SerializedName("estimatedCost")
    val estimatedCost: Long,
    @SerializedName("reason")
    val reason: String,
    @SerializedName("place")
    val place: PlaceDto,
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
