package com.example.mobileproject.domain.usecase

import com.example.mobileproject.domain.entity.ExplorePlan
import com.example.mobileproject.domain.repository.PlaceRepository
import javax.inject.Inject

/**
 * Use case lập kế hoạch khám phá (explore plan) cho cặp đôi.
 *
 * Tính năng "Đi đâu?" — tự động tạo lịch trình đi chơi dựa trên:
 * - Ngân sách và số người tham gia
 * - Vị trí hiện tại và bán kính tìm kiếm
 * - Loại địa điểm, đánh giá tối thiểu
 *
 * Hỗ trợ cá nhân hóa bằng cách truyền vào các danh sách ID đã xem/đã đi/đã gửi
 * và từ khóa tìm kiếm gần đây, giúp thuật toán không gợi ý trùng lặp.
 *
 * Use case này tồn tại vì tham số quá nhiều (17 params) — việc gọi trực tiếp
 * repository từ ViewModel sẽ gây rối. Use case đóng vai trò facade đơn giản hóa.
 */
class GetExplorePlanUseCase @Inject constructor(
    private val placeRepository: PlaceRepository,
) {
    suspend operator fun invoke(
        randomSeed: Long? = null,
        budget: Long,
        peopleCount: Int,
        desiredStops: Int,
        query: String? = null,
        province: String? = null,
        district: String? = null,
        type: String = "all",
        minRating: Double? = null,
        nearLat: Double? = null,
        nearLng: Double? = null,
        radiusKm: Double? = null,
        excludePlaceIds: List<String> = emptyList(),
        viewedPlaceIds: List<String> = emptyList(),
        gonePlaceIds: List<String> = emptyList(),
        sentPlaceIds: List<String> = emptyList(),
        recentKeywords: List<String> = emptyList(),
    ): ExplorePlan {
        return placeRepository.getExplorePlan(
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
        )
    }
}
