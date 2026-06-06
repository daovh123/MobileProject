package com.example.mobileproject.domain.usecase

import com.example.mobileproject.domain.entity.Place
import com.example.mobileproject.domain.repository.PlaceRepository
import javax.inject.Inject

/**
 * Use case lấy một địa điểm ngẫu nhiên phù hợp với tiêu chí.
 *
 * Dùng cho tính năng "Đi đâu?" khi cặp đôi muốn một gợi ý nhanh
 * mà không cần xem toàn bộ danh sách. Hữu ích khi cả hai đều
 * không biết đi đâu và muốn "quay số"随机 địa điểm.
 */
class GetRandomPlaceUseCase @Inject constructor(
    private val placeRepository: PlaceRepository,
) {
    suspend operator fun invoke(
        query: String?,
        province: String?,
        district: String?,
        type: String,
        minRating: Double?,
        nearLat: Double? = null,
        nearLng: Double? = null,
        radiusKm: Double? = null,
    ): Place {
        return placeRepository.getRandomPlace(
            query = query,
            province = province,
            district = district,
            type = type,
            minRating = minRating,
            nearLat = nearLat,
            nearLng = nearLng,
            radiusKm = radiusKm,
        )
    }
}