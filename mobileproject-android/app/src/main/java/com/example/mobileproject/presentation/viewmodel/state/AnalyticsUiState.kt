package com.example.mobileproject.presentation.viewmodel.state

import com.example.mobileproject.domain.entity.CategoryBreakdown
import com.example.mobileproject.domain.entity.SpendingTrend

data class AnalyticsUiState(
    val isLoading: Boolean = false,
    val categoryBreakdown: List<CategoryBreakdown> = emptyList(),
    val spendingTrend: List<SpendingTrend> = emptyList(),
    val error: String? = null
)