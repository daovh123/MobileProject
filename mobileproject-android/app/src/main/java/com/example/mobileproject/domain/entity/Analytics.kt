package com.example.mobileproject.domain.entity

data class CategoryBreakdown(
    val category: String,
    val totalAmount: Long
)

data class SpendingTrend(
    val date: Int,
    val totalAmount: Long
)