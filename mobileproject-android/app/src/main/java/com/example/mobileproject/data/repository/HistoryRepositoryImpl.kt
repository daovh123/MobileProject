package com.example.mobileproject.data.repository

import com.example.mobileproject.data.datasource.remote.ApiService
import com.example.mobileproject.data.mapper.toDomain
import com.example.mobileproject.domain.entity.Place
import com.example.mobileproject.domain.repository.HistoryRepository
import javax.inject.Inject

/**
 * Implementation của [HistoryRepository], quản lý lịch sử xem địa điểm.
 *
 * ## Caching strategy
 * Không có cache local, tất cả dữ liệu lấy từ API.
 *
 * ## Error handling
 * Sử dụng `runCatching` + [Result] để bắt exception.
 * Kiểm tra HTTP status code và body trước khi xử lý.
 *
 * ## Data transformation
 * Chuyển đổi `HistoryPlaceDto` -> [Place] (domain entity) qua mapper `toDomain()`.
 *
 * ## Threading
 * Tất cả hàm `suspend` chạy trên IO dispatcher (Retrofit default).
 */
class HistoryRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
) : HistoryRepository {

    /**
     * Ghi nhận lượt xem địa điểm vào lịch sử.
     *
     * Gọi mỗi khi người dùng xem chi tiết một địa điểm.
     *
     * @param token JWT token
     * @param placeId ID địa điểm đã xem
     * @return Result<Unit>
     */
    override suspend fun recordView(token: String, placeId: String): Result<Unit> {
        return runCatching {
            val response = apiService.recordHistory(authorizationHeader(token), placeId)
            if (!response.isSuccessful) {
                throw IllegalStateException("Record history failed")
            }
        }
    }

    /**
     * Lấy danh sách lịch sử xem địa điểm.
     *
     * @param token JWT token
     * @return Result<List<Place>> danh sách địa điểm đã xem
     */
    override suspend fun getHistory(token: String): Result<List<Place>> {
        return runCatching {
            val response = apiService.getHistory(authorizationHeader(token))
            val body = response.body()

            if (!response.isSuccessful || body == null || !body.success) {
                throw IllegalStateException(body?.message ?: "Get history failed")
            }

            body.places.map { it.toDomain() }
        }
    }

    /**
     * Xóa toàn bộ lịch sử xem địa điểm.
     *
     * @param token JWT token
     * @return Result<Unit>
     */
    override suspend fun clearHistory(token: String): Result<Unit> {
        return runCatching {
            val response = apiService.clearHistory(authorizationHeader(token))
            if (!response.isSuccessful) {
                throw IllegalStateException("Clear history failed")
            }
        }
    }

    /**
     * Tạo header xác thực từ token.
     *
     * @param token JWT token
     * @return "Bearer <token>"
     */
    private fun authorizationHeader(token: String): String = "Bearer ${token.trim()}"
}
