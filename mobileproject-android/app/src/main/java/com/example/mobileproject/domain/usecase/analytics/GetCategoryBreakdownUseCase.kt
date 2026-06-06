package com.example.mobileproject.domain.usecase.analytics

import com.example.mobileproject.domain.entity.CategoryBreakdown
import com.example.mobileproject.domain.repository.AnalyticsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case lấy thống kê chi tiêu theo danh mục trong một tháng cụ thể.
 *
 * Dữ liệu được dùng để vẽ biểu đồ tròn (pie chart) trên màn hình Analytics,
 * giúp cặp đôi nhận biết tỷ lệ chi tiêu giữa các danh mục
 * (ăn uống chiếm bao nhiêu %, giải trí bao nhiêu %, v.v.)
 */
class GetCategoryBreakdownUseCase @Inject constructor(
    private val repository: AnalyticsRepository
) {
    suspend operator fun invoke(coupleId: String, month: Int, year: Int): Flow<Result<List<CategoryBreakdown>>> {
        return repository.getCategoryBreakdown(coupleId, month, year)
    }
}