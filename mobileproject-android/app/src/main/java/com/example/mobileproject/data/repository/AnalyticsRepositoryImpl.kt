package com.example.mobileproject.data.repository

import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.data.datasource.remote.ApiService
import com.example.mobileproject.data.mapper.toEntity
import com.example.mobileproject.domain.entity.CategoryBreakdown
import com.example.mobileproject.domain.entity.SpendingTrend
import com.example.mobileproject.domain.repository.AnalyticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Implementation của [AnalyticsRepository], xử lý dữ liệu phân tích chi tiêu.
 *
 * ## Caching strategy
 * Không có cache, mỗi lần gọi tạo Flow mới và fetch từ API.
 * Dữ liệu analytics thay đổi theo thời gian thực nên không phù hợp cache local.
 *
 * ## Data transformation
 * - [CategoryBreakdownDto] -> [CategoryBreakdown]: tính tổng và phần trăm cho mỗi danh mục
 * - [SpendingTrendDto] -> [SpendingTrend]: map trực tiếp
 *
 * ## Error handling
 * Trả về [Result.failure] khi API call thất bại hoặc response không thành công.
 *
 * ## Threading
 * Flow chạy trên dispatcher của người gọi (thường là IO dispatcher).
 */
class AnalyticsRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val authSessionStore: AuthSessionStore
) : AnalyticsRepository {

    /**
     * Tạo header xác thực từ token đã lưu.
     *
     * @return "Bearer <token>"
     */
    private fun authorizationHeader(): String {
        val token = authSessionStore.load()?.token.orEmpty()
        return "Bearer $token"
    }

    /**
     * Lấy phân bổ chi tiêu theo danh mục trong tháng.
     *
     * Tính tổng `totalAmount` từ tất cả danh mục, sau đó map mỗi DTO
     * thành entity với phần trăm tương ứng (percentage).
     *
     * @param coupleId ID cặp đôi
     * @param month tháng (1-12)
     * @param year năm
     * @return Flow<Result<List<CategoryBreakdown>>>
     */
    override suspend fun getCategoryBreakdown(
        coupleId: String,
        month: Int,
        year: Int
    ): Flow<Result<List<CategoryBreakdown>>> = flow {
        try {
            val response = apiService.getCategoryBreakdown(authorizationHeader(), coupleId, month, year)
            if (response.isSuccessful && response.body() != null) {
                val dtos = response.body()!!
                val totalSum = dtos.sumOf { it.totalAmount }
                val entities = dtos.map { it.toEntity(totalSum) }
                emit(Result.success(entities))
            } else {
                emit(Result.failure(Exception(response.message())))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Lấy xu hướng chi tiêu theo tháng trong năm.
     *
     * API chỉ cần `year` (không cần `month`), trả về dữ liệu 12 tháng.
     * Tham số `month` được giữ để tương thích interface nhưng không sử dụng.
     *
     * @param coupleId ID cặp đôi
     * @param month tháng (không sử dụng, API chỉ dùng year)
     * @param year năm
     * @return Flow<Result<List<SpendingTrend>>>
     */
    override suspend fun getSpendingTrend(
        coupleId: String,
        month: Int,
        year: Int
    ): Flow<Result<List<SpendingTrend>>> = flow {
        try {
            // Sửa lại để gọi getMonthlyTrend chỉ với year theo API mới
            val response = apiService.getMonthlyTrend(authorizationHeader(), coupleId, year)
            if (response.isSuccessful && response.body() != null) {
                val dtos = response.body()!!
                val entities = dtos.map { it.toEntity() }
                emit(Result.success(entities))
            } else {
                emit(Result.failure(Exception(response.message())))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
