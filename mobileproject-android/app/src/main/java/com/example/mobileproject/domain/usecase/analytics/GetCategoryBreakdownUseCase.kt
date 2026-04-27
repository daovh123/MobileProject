package com.example.mobileproject.domain.usecase.analytics

import com.example.mobileproject.domain.entity.CategoryBreakdown
import com.example.mobileproject.domain.repository.AnalyticsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCategoryBreakdownUseCase @Inject constructor(
    private val repository: AnalyticsRepository
) {
    suspend operator fun invoke(coupleId: String, month: Int, year: Int): Flow<Result<List<CategoryBreakdown>>> {
        return repository.getCategoryBreakdown(coupleId, month, year)
    }
}