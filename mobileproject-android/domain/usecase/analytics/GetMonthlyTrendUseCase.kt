package com.example.mobileproject.domain.usecase.analytics

import com.example.mobileproject.domain.entity.SpendingTrend
import com.example.mobileproject.domain.repository.AnalyticsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case lấy xu hướng chi tiêu hàng tháng của cặp đôi.
 *
 * Phiên bản trong domain module, nhận cả [year] và [month] làm tham số
 * nhưng repository implementation mới chỉ sử dụng [year] để trả về
 * dữ liệu xu hướng theo cả năm. Tham số [month] được truyền qua
 * nhưng không được sử dụng trong repository.getSpendingTrend().
 *
 * Dữ liệu [SpendingTrend] chứa mốc thời gian và tổng chi tiêu,
 * được dùng để vẽ biểu đồ đường xu hướng chi tiêu.
 */
class GetMonthlyTrendUseCase @Inject constructor(
    private val repository: AnalyticsRepository
) {
    suspend operator fun invoke(coupleId: String, year: Int, month: Int): Flow<Result<List<SpendingTrend>>> {
        return repository.getSpendingTrend(coupleId, year, month)
    }
}