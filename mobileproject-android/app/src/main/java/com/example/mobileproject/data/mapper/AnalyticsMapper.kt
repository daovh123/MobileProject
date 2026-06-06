package com.example.mobileproject.data.mapper

import com.example.mobileproject.data.model.analytics.CategoryBreakdownDto
import com.example.mobileproject.data.model.analytics.SpendingTrendDto
import com.example.mobileproject.domain.entity.CategoryBreakdown
import com.example.mobileproject.domain.entity.SpendingTrend

/**
 * Ánh xạ [CategoryBreakdownDto] sang [CategoryBreakdown] domain entity.
 *
 * Tính toán thêm [percentage] dựa trên [totalSum]:
 * - percentage = totalAmount / totalSum * 100
 * - Nếu [totalSum] = 0 (tránh chia cho 0), percentage = 0f
 *
 * @param totalSum Tổng chi tiêu của tất cả danh mục, dùng làm mẫu số tính phần trăm.
 */
fun CategoryBreakdownDto.toEntity(totalSum: Long): CategoryBreakdown {
    return CategoryBreakdown(
        category = this.category,
        totalAmount = this.totalAmount,
        percentage = if (totalSum > 0) (this.totalAmount.toFloat() / totalSum) else 0f
    )
}

/**
 * Ánh xạ [SpendingTrendDto] sang [SpendingTrend] domain entity.
 *
 * Mapping 1:1, không có transformation hay default value.
 * [month] giữ nguyên giá trị 1-12 từ API.
 */
fun SpendingTrendDto.toEntity(): SpendingTrend {
    return SpendingTrend(
        month = this.month,
        totalIncome = this.totalIncome,
        totalExpense = this.totalExpense
    )
}
