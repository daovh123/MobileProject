package com.example.mobileproject.domain.usecase

import com.example.mobileproject.domain.entity.PlaceSearchPage
import com.example.mobileproject.domain.repository.PlaceRepository
import javax.inject.Inject

/**
 * Use case tìm kiếm địa điểm với phân trang và nhiều tiêu chí lọc.
 *
 * Cung cấp giao diện tìm kiếm linh hoạt cho người dùng:
 * theo từ khóa, tỉnh/quận, loại địa điểm, đánh giá, vị trí địa lý.
 * Hỗ trợ phân trang và sắp xếp (trending, rating, distance).
 */
class SearchPlacesUseCase @Inject constructor(
    private val placeRepository: PlaceRepository,
) {
    suspend operator fun invoke(
        query: String?,
        province: String? = null,
        district: String? = null,
        type: String = "all",
        minRating: Double? = null,
        nearLat: Double? = null,
        nearLng: Double? = null,
        radiusKm: Double? = null,
        page: Int = 0,
        size: Int = 20,
        sort: String = "trending",
    ): PlaceSearchPage {
        return placeRepository.searchPlaces(
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
}