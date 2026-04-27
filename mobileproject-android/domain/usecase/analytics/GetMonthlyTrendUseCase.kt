package com.example.mobileproject.domain.usecase.analytics

import com.example.mobileproject.domain.entity.SpendingTrend
import com.example.mobileproject.domain.repository.AnalyticsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMonthlyTrendUseCase @Inject constructor(
    private val repository: AnalyticsRepository
) {
    suspend operator fun invoke(coupleId: String, year: Int, month: Int): Flow<Result<List<SpendingTrend>>> {
        return repository.getSpendingTrend(coupleId, year, month)
    }
}