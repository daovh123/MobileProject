package com.mobileproject.mobileprojectbackend.analytics.dto;

/**
 * DTO kết quả phân tích chi tiêu theo danh mục.
 * Kết quả từ MongoDB aggregation: group by {@code category}, sum {@code amount}.
 *
 * @param category    tên danh mục chi tiêu
 * @param totalAmount tổng số tiền chi tiêu trong danh mục (VND)
 */
public record CategoryBreakdownItem(
        String category,
        Long totalAmount
) {
}