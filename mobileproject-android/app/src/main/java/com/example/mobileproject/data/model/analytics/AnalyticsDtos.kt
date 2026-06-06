package com.example.mobileproject.data.model.analytics

import com.google.gson.annotations.SerializedName

/**
 * DTO thống kê chi tiêu theo danh mục.
 *
 * Dùng cho biểu đồ phân bổ chi tiêu (pie chart).
 * Giá trị [totalAmount] là tổng chi tiêu trong một danh mục cụ thể.
 */
data class CategoryBreakdownDto(
    /** Tên danh mục chi tiêu (VD: "Food", "Transport", "Shopping"). */
    @SerializedName("category") val category: String,
    /** Tổng số tiền đã chi trong danh mục này (VNĐ). */
    @SerializedName("totalAmount") val totalAmount: Long
)

/**
 * DTO xu hướng chi tiêu theo tháng.
 *
 * Dùng cho biểu đồ xu hướng thu/chi (line chart hoặc bar chart).
 * [month] là số tháng (1-12), kết hợp với năm từ context ngoài.
 */
data class SpendingTrendDto(
    /** Số tháng (1-12). */
    @SerializedName("month") val month: Int,
    /** Tổng thu nhập trong tháng (VNĐ). */
    @SerializedName("totalIncome") val totalIncome: Long,
    /** Tổng chi tiêu trong tháng (VNĐ). */
    @SerializedName("totalExpense") val totalExpense: Long
)
