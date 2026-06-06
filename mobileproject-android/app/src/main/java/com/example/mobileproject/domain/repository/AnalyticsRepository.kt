package com.example.mobileproject.domain.repository

import com.example.mobileproject.domain.entity.CategoryBreakdown
import com.example.mobileproject.domain.entity.SpendingTrend
import kotlinx.coroutines.flow.Flow

/**
 * Repository cung cấp dữ liệu phân tích tài chính (analytics) cho cặp đôi.
 *
 * Bao gồm phân tích theo danh mục chi tiêu và xu hướng chi tiêu theo thời gian,
 * giúp cặp đôi hiểu rõ thói quen tài chính và đưa ra quyết định chi tiêu tốt hơn.
 */
interface AnalyticsRepository {

    /**
     * Lấy thống kê chi tiêu phân theo danh mục trong một tháng cụ thể.
     *
     * Dùng cho biểu đồ tròn (pie chart) hiển thị tỷ lệ chi tiêu
     * giữa các danh mục như ăn uống, giải trí, hóa đơn...
     *
     * @param coupleId ID duy nhất của cặp đôi
     * @param month Tháng cần thống kê (1-12)
     * @param year Năm cần thống kê
     * @return Flow phát ra [Result.success] chứa danh sách [CategoryBreakdown],
     *         mỗi phần tử đại diện cho tổng chi tiêu của một danh mục
     * @throws IllegalArgumentException khi month không nằm trong khoảng 1-12
     */
    suspend fun getCategoryBreakdown(
        coupleId: String,
        month: Int,
        year: Int
    ): Flow<Result<List<CategoryBreakdown>>>

    /**
     * Lấy xu hướng chi tiêu theo thời gian (trend line).
     *
     * Dùng cho biểu đồ đường (line chart) hiển diễn biến chi tiêu
     * qua các ngày/tháng, giúp nhận biết pattern chi tiêu và dự đoán tương lai.
     *
     * @param coupleId ID duy nhất của cặp đôi
     * @param month Tháng cần thống kê (1-12), có thể không dùng trong một số implementation
     * @param year Năm cần thống kê
     * @return Flow phát ra [Result.success] chứa danh sách [SpendingTrend],
     *         mỗi phần tử đại diện cho chi tiêu tại một mốc thời gian
     */
    suspend fun getSpendingTrend(
        coupleId: String,
        month: Int,
        year: Int
    ): Flow<Result<List<SpendingTrend>>>
}