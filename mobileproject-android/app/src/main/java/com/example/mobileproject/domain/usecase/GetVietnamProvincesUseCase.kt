package com.example.mobileproject.domain.usecase

import com.example.mobileproject.domain.repository.PlaceRepository
import javax.inject.Inject

/**
 * Use case lấy danh sách tỉnh/thành phố Việt Nam.
 *
 * Dùng để populate dropdown chọn tỉnh/thành trong bộ lọc địa điểm.
 */
class GetVietnamProvincesUseCase @Inject constructor(
    private val placeRepository: PlaceRepository,
) {
    suspend operator fun invoke(): List<String> {
        return placeRepository.getVietnamProvinces()
    }
}
