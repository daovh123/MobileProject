package com.example.mobileproject.data.mapper

import com.example.mobileproject.data.model.place.PlaceDto
import com.example.mobileproject.data.model.place.ExplorePlanResponseDto
import com.example.mobileproject.data.model.place.PlaceFilterOptionsDto
import com.example.mobileproject.data.model.place.VietnamProvinceDto
import com.example.mobileproject.domain.entity.ExplorePlan
import com.example.mobileproject.domain.entity.ExplorePlanItem
import com.example.mobileproject.domain.entity.ExplorePlanSummary
import com.example.mobileproject.domain.entity.PlaceFilterOptions
import com.example.mobileproject.domain.entity.Place
import java.util.Locale

/**
 * Ánh xạ [PlaceDto] sang [Place] domain entity.
 *
 * Logic đặc biệt:
 * - [effectiveTag] ưu tiên dùng giá trị từ API, fallback về [category] nếu null.
 *   Điều này đảm bảo UI luôn có tag hiển thị cho người dùng.
 * - Các trường nullable được giữ nguyên nullable trong domain entity.
 */
fun PlaceDto.toDomain(): Place {
    return Place(
        id = id,
        name = name,
        address = address,
        district = district,
        province = province,
        effectiveTag = effectiveTag ?: category,
        category = category,
        mealType = mealType,
        rating = rating,
        reviewCount = reviewCount,
        openHours = openHours,
        priceRange = priceRange,
        imageUrl = imageUrl,
        googleMapsUrl = googleMapsUrl,
        lat = lat,
        lng = lng,
    )
}

/**
 * Ánh xạ [PlaceFilterOptionsDto] sang [PlaceFilterOptions] domain entity.
 *
 * Mapping 1:1, danh sách quận/huyện và tỉnh/thành phố được giữ nguyên.
 */
fun PlaceFilterOptionsDto.toDomain(): PlaceFilterOptions {
    return PlaceFilterOptions(
        districts = districts,
        provinces = provinces,
    )
}

/**
 * Chuyển đổi danh sách [VietnamProvinceDto] thành danh sách tên tỉnh đã chuẩn hóa.
 *
 * Xử lý:
 * - Loại bỏ tên null hoặc rỗng
 * - Trim khoảng trắng
 * - Loại bỏ trùng lặp (case-insensitive)
 * - Sắp xếp theo alphabet (case-insensitive)
 */
fun List<VietnamProvinceDto>.toProvinceNames(): List<String> {
    return asSequence()
        .mapNotNull { it.name?.trim() }
        .filter { it.isNotBlank() }
        .distinctBy { it.lowercase(Locale.ROOT) }
        .sortedBy { it.lowercase(Locale.ROOT) }
        .toList()
}

/**
 * Ánh xạ [ExplorePlanResponseDto] sang [ExplorePlan] domain entity.
 *
 * Mapping đệ quy: mỗi [ExplorePlanItemDto] sẽ được chuyển thành [ExplorePlanItem],
 * trong đó [PlaceDto] con cũng được chuyển qua [PlaceDto.toDomain()].
 */
fun ExplorePlanResponseDto.toDomain(): ExplorePlan {
    return ExplorePlan(
        summary = ExplorePlanSummary(
            totalBudget = summary.totalBudget,
            peopleCount = summary.peopleCount,
            desiredStops = summary.desiredStops,
            estimatedTotalCost = summary.estimatedTotalCost,
            lowBalance = summary.lowBalance,
            balanceMessage = summary.balanceMessage,
            suggestedDefaultBudget = summary.suggestedDefaultBudget,
        ),
        items = items.map {
            ExplorePlanItem(
                stopOrder = it.stopOrder,
                experienceType = it.experienceType,
                estimatedCost = it.estimatedCost,
                reason = it.reason,
                place = it.place.toDomain(),
            )
        },
    )
}
