package com.example.mobileproject.data.model.analytics

import com.google.gson.annotations.SerializedName

data class CategoryBreakdownDto(
    @SerializedName("category") val category: String,
    @SerializedName("totalAmount") val totalAmount: Long
)

data class SpendingTrendDto(
    @SerializedName("month") val month: Int,
    @SerializedName("totalIncome") val totalIncome: Long,
    @SerializedName("totalExpense") val totalExpense: Long
)
