package com.example.mobileproject.data.mapper

import com.example.mobileproject.data.model.analytics.CategoryBreakdownDto
import com.example.mobileproject.data.model.analytics.SpendingTrendDto
import com.example.mobileproject.domain.entity.CategoryBreakdown
import com.example.mobileproject.domain.entity.SpendingTrend

fun CategoryBreakdownDto.toEntity(totalSum: Long): CategoryBreakdown {
    return CategoryBreakdown(
        category = this.category,
        totalAmount = this.totalAmount,
        percentage = if (totalSum > 0) (this.totalAmount.toFloat() / totalSum) else 0f
    )
}

fun SpendingTrendDto.toEntity(): SpendingTrend {
    return SpendingTrend(
        month = this.month,
        totalIncome = this.totalIncome,
        totalExpense = this.totalExpense
    )
}
