package com.mobileproject.mobileprojectbackend.analytics.dto;

/**
 * DTO kết quả xu hướng chi tiêu theo ngày trong tháng.
 * Kết quả từ MongoDB aggregation: group theo ngày, sum EXPENSE amount.
 *
 * @param date        số thứ tự ngày trong tháng (1–31)
 * @param totalAmount tổng chi tiêu trong ngày (VND), 0 nếu không có giao dịch
 */
public record SpendingTrendItem(
        int date,
        Long totalAmount
) {
}