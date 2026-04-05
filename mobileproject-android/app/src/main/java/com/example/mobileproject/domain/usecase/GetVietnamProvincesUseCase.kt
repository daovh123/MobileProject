package com.example.mobileproject.domain.usecase

import com.example.mobileproject.domain.repository.PlaceRepository
import javax.inject.Inject

class GetVietnamProvincesUseCase @Inject constructor(
    private val placeRepository: PlaceRepository,
) {
    suspend operator fun invoke(): List<String> {
        return placeRepository.getVietnamProvinces()
    }
}
