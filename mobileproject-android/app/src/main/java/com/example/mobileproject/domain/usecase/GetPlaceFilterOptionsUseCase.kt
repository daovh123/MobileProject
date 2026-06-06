package com.example.mobileproject.domain.usecase

import com.example.mobileproject.domain.entity.PlaceFilterOptions
import com.example.mobileproject.domain.repository.PlaceRepository
import javax.inject.Inject

/**
 * Use case lấy danh sách tùy chọn lọc địa điểm.
 *
 * Trả về các tùy chọn có sẵn (tỉnh/thành, quận/huyện, loại địa điểm)
 * để populate bộ lọc trên UI tìm kiếm địa điểm.
 */
class GetPlaceFilterOptionsUseCase @Inject constructor(
    private val placeRepository: PlaceRepository,
) {
    suspend operator fun invoke(): PlaceFilterOptions {
        return placeRepository.getFilterOptions()
    }
}