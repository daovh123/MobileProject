package com.example.mobileproject.domain.repository

import com.example.mobileproject.domain.entity.Place

/**
 * Repository quản lý lịch sử xem địa điểm (view history) của người dùng.
 *
 * Ghi nhận tự động mỗi khi người dùng xem chi tiết một địa điểm,
 * giúp hiển thị "Đã xem gần đây" và hỗ trợ thuật toán gợi ý cá nhân hóa.
 */
interface HistoryRepository {

    /**
     * Ghi nhận lượt xem một địa điểm.
     *
     * Được gọi mỗi khi người dùng mở màn hình chi tiết địa điểm.
     * Dữ liệu này dùng để: hiển thị lịch sử xem, gợi ý cá nhân hóa,
     * và đánh dấu "đã xem" trong explore plan.
     *
     * @param token Token xác thực
     * @param placeId ID địa điểm đã xem
     * @return [Result.success] khi ghi nhận thành công
     */
    suspend fun recordView(token: String, placeId: String): Result<Unit>

    /**
     * Lấy danh sách địa điểm đã xem gần đây.
     *
     * @param token Token xác thực
     * @return [Result.success] chứa danh sách [Place] sắp xếp theo thời gian xem giảm dần
     */
    suspend fun getHistory(token: String): Result<List<Place>>

    /**
     * Xóa toàn bộ lịch sử xem địa điểm.
     *
     * @param token Token xác thực
     * @return [Result.success] khi xóa thành công
     */
    suspend fun clearHistory(token: String): Result<Unit>
}
