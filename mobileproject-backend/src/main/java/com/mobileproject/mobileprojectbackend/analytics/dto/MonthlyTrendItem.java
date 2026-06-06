package com.mobileproject.mobileprojectbackend.analytics.dto;

/**
 * DTO kết quả xu hướng thu/chi theo tháng.
 * Kết quả từ MongoDB aggregation: group theo (month, type), sum amount.
 *
 * @param month        số thứ tự tháng (1–12)
 * @param totalIncome  tổng thu nhập trong tháng (VND)
 * @param totalExpense tổng chi tiêu trong tháng (VND)
 */
public record MonthlyTrendItem(
        int month,
        Long totalIncome,
        Long totalExpense
) {
}