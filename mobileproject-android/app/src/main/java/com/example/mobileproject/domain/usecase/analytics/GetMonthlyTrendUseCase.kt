package com.example.mobileproject.domain.usecase.analytics

import com.example.mobileproject.domain.entity.SpendingTrend
import com.example.mobileproject.domain.repository.AnalyticsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case lấy xu hướng chi tiêu theo từng tháng trong năm.
 *
 * Dữ liệu được dùng để vẽ biểu đồ đường (line chart) trên màn hình Analytics,
 * giúp cặp đôi theo dõi diễn biến chi tiêu qua các tháng,
 * nhận biết tháng nào chi nhiều/tháng nào chi ít để điều chỉnh thói quen.
 */
class GetMonthlyTrendUseCase @Inject constructor(
    private val repository: AnalyticsRepository
) {
    suspend operator fun invoke(coupleId: String, year: Int): Flow<Result<List<SpendingTrend>>> {
        return repository.getSpendingTrend(coupleId, 0, year) // month không dùng trong repository mới
    }
}
