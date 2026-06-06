package com.example.mobileproject.data.repository

import com.example.mobileproject.data.datasource.remote.ApiService
import com.example.mobileproject.data.mapper.toDomain
import com.example.mobileproject.domain.entity.Place
import com.example.mobileproject.domain.repository.FavoriteRepository
import javax.inject.Inject

/**
 * Implementation của [FavoriteRepository], quản lý danh sách địa điểm yêu thích.
 *
 * ## Caching strategy
 * Không có cache local, tất cả dữ liệu lấy từ API.
 * Toggle favorite dựa vào response message (chứa "Added" = đã thêm).
 *
 * ## Error handling
 * Sử dụng `runCatching` + [Result] để bắt exception và trả về failure.
 * Kiểm tra `response.isSuccessful`, `body != null`, và `body.success` trước khi xử lý.
 *
 * ## Data transformation
 * Chuyển đổi `FavoritePlaceDto` -> [Place] (domain entity) qua mapper `toDomain()`.
 *
 * ## Threading
 * Tất cả hàm `suspend` chạy trên IO dispatcher (Retrofit default).
 */
class FavoriteRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
) : FavoriteRepository {

    /**
     * Thêm/xóa địa điểm yêu thích (toggle).
     *
     * Xác định trạng thái mới dựa vào response message:
     * - Nếu message chứa "Added" -> trả về `true` (đã thêm)
     * - Ngược lại -> trả về `false` (đã xóa)
     *
     * @param token JWT token
     * @param placeId ID địa điểm
     * @return Result<Boolean> `true` nếu đã thêm, `false` nếu đã xóa
     */
    override suspend fun toggleFavorite(token: String, placeId: String): Result<Boolean> {
        return runCatching {
            val response = apiService.toggleFavorite(authorizationHeader(token), placeId)
            val body = response.body()

            if (!response.isSuccessful || body == null || !body.success) {
                throw IllegalStateException(body?.message ?: "Toggle favorite failed")
            }

            body.message.contains("Added", ignoreCase = true)
        }
    }

    /**
     * Lấy danh sách địa điểm yêu thích.
     *
     * @param token JWT token
     * @return Result<List<Place>> danh sách địa điểm yêu thích
     */
    override suspend fun getFavorites(token: String): Result<List<Place>> {
        return runCatching {
            val response = apiService.getFavorites(authorizationHeader(token))
            val body = response.body()

            if (!response.isSuccessful || body == null || !body.success) {
                throw IllegalStateException(body?.message ?: "Get favorites failed")
            }

            body.places.map { it.toDomain() }
        }
    }

    /**
     * Kiểm tra một địa điểm có trong danh sách yêu thích không.
     *
     * Dùng để hiển thị đúng icon trái tim trên UI khi xem chi tiết địa điểm.
     *
     * @param token JWT token
     * @param placeId ID địa điểm
     * @return Result<Boolean> `true` nếu là yêu thích
     */
    override suspend fun checkFavorite(token: String, placeId: String): Result<Boolean> {
        return runCatching {
            val response = apiService.checkFavorite(authorizationHeader(token), placeId)
            val body = response.body() ?: throw IllegalStateException("Check favorite failed")

            if (!response.isSuccessful) {
                throw IllegalStateException(body.message)
            }

            body.success
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
