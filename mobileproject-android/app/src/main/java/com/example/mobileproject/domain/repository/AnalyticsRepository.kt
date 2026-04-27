package com.example.mobileproject.domain.repository

import com.example.mobileproject.domain.entity.CategoryBreakdown
import com.example.mobileproject.domain.entity.SpendingTrend
import kotlinx.coroutines.flow.Flow

interface AnalyticsRepository {
    suspend fun getCategoryBreakdown(
        coupleId: String,
        month: Int,
        year: Int
    ): Flow<Result<List<CategoryBreakdown>>>

    suspend fun getSpendingTrend(
        coupleId: String,
        month: Int,
        year: Int
    ): Flow<Result<List<SpendingTrend>>>
}