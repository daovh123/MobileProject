package com.example.mobileproject.domain.repository

import com.example.mobileproject.core.result.Resource
import com.example.mobileproject.domain.entity.CategoryBreakdown
import com.example.mobileproject.domain.entity.SpendingTrend

interface AnalyticsRepository {
    suspend fun getCategoryBreakdown(
        coupleId: String,
        startDate: String,
        endDate: String
    ): Resource<List<CategoryBreakdown>>

    suspend fun getSpendingTrend(
        coupleId: String,
        year: Int,
        month: Int
    ): Resource<List<SpendingTrend>>
}