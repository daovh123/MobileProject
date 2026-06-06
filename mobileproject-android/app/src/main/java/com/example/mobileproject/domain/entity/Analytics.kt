package com.example.mobileproject.domain.entity

/**
 * Domain entity chứa dữ liệu phân tích chi tiêu theo danh mục.
 * Được sử dụng để hiển thị biểu đồ tròn (pie chart) phân bổ chi tiêu.
 *
 * @property category Tên danh mục chi tiêu (ví dụ: "Ăn uống", "Di chuyển", "Giải trí")
 * @property amount Tong so tien da chi trong danh muc nay (don vi: VND)
 * @property percentage Ty le phần trăm so với tổng chi tiêu, tự động tính từ server hoặc client
 */
data class CategoryBreakdown(
    val category: String,
    val totalAmount: Long,
    val percentage: Float = 0f
)

/**
 * Domain entity chứa dữ liệu xu hướng chi tiêu theo tháng.
 * Được sử dụng để hiển thị biểu đồ đường (line chart) so sánh thu nhập và chi tiêu qua các tháng.
 *
 * @property month Số tháng (1-12), đại diện cho tháng trong năm
 * @property totalIncome Tổng thu nhập trong tháng (đơn vị: VNĐ)
 * @property totalExpense Tổng chi tiêu trong tháng (đơn vị: VNĐ)
 */
data class SpendingTrend(
    val month: Int,
    val totalIncome: Long,
    val totalExpense: Long
)

/**
 * Domain entity tổng hợp báo cáo phân tích tài chính của cặp đôi.
 * Kết hợp dữ liệu phân bổ theo danh mục và xu hướng theo thời gian.
 *
 * @property categoryBreakdown Danh sách chi tiêu phân theo từng danh mục
 * @property spendingTrend Danh sách xu hướng thu chi theo từng tháng
 */
data class AnalyticsReport(
    val categoryBreakdown: List<CategoryBreakdown>,
    val spendingTrend: List<SpendingTrend>
)
