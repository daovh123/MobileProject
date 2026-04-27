package com.example.mobileproject.domain.entity

data class CategoryBreakdown(
    val category: String,
    val totalAmount: Long,
    val percentage: Float = 0f
)

data class SpendingTrend(
    val month: Int,
    val totalIncome: Long,
    val totalExpense: Long
)

data class AnalyticsReport(
    val categoryBreakdown: List<CategoryBreakdown>,
    val spendingTrend: List<SpendingTrend>
)
