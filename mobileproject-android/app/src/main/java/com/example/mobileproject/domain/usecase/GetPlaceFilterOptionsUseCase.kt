package com.example.mobileproject.domain.usecase

import com.example.mobileproject.domain.entity.PlaceFilterOptions
import com.example.mobileproject.domain.repository.PlaceRepository
import javax.inject.Inject

class GetPlaceFilterOptionsUseCase @Inject constructor(
    private val placeRepository: PlaceRepository,
) {
    suspend operator fun invoke(): PlaceFilterOptions {
        return placeRepository.getFilterOptions()
    }
}