package com.example.mobileproject.presentation.viewmodel.state

import com.example.mobileproject.domain.entity.CategoryBreakdown
import com.example.mobileproject.domain.entity.SpendingTrend

/**
 * Trạng thái UI cho màn hình Phân tích chi tiêu (Analytics).
 *
 * @property isLoading True khi đang tải dữ liệu phân tích
 * @property categoryBreakdown Phân tích chi tiêu theo danh mục cho tháng đã chọn.
 *   Sắp xếp theo tổng tiền giảm dần. UI dùng để render biểu đồ tròn và danh sách
 * @property spendingTrend Xu hướng chi tiêu theo tháng trong năm.
 *   Mỗi item chứa tháng và tổng tiền. UI dùng để render line chart xu hướng
 * @property error Thông báo lỗi (null nếu không có lỗi)
 */
data class AnalyticsUiState(
    val isLoading: Boolean = false,
    val categoryBreakdown: List<CategoryBreakdown> = emptyList(),
    val spendingTrend: List<SpendingTrend> = emptyList(),
    val error: String? = null
)