package com.example.mobileproject.domain.repository

import com.example.mobileproject.domain.entity.Place

/**
 * Repository quản lý danh sách địa điểm yêu thích (favorites) của người dùng.
 *
 * Cho phép người dùng lưu lại các địa điểm quan tâm để dễ dàng truy cập sau này.
 * Sử dụng cơ chế toggle để chuyển đổi trạng thái yêu thích thuận tiện.
 */
interface FavoriteRepository {

    /**
     * Chuyển đổi trạng thái yêu thích của một địa điểm (toggle).
     *
     * Nếu địa điểm đã yêu thích → bỏ yêu thích, và ngược lại.
     *
     * @param token Token xác thực
     * @param placeId ID địa điểm cần toggle
     * @return [Result.success] chứa true nếu địa điểm hiện đang là yêu thích,
     *         false nếu đã bỏ yêu thích
     */
    suspend fun toggleFavorite(token: String, placeId: String): Result<Boolean>

    /**
     * Lấy danh sách tất cả địa điểm đã yêu thích.
     *
     * @param token Token xác thực
     * @return [Result.success] chứa danh sách [Place] yêu thích,
     *         [Result.failure] khi có lỗi
     */
    suspend fun getFavorites(token: String): Result<List<Place>>

    /**
     * Kiểm tra một địa điểm có đang được yêu thích hay không.
     *
     * Hữu ích để cập nhật icon trái tim trên UI mà không cần tải toàn bộ danh sách.
     *
     * @param token Token xác thực
     * @param placeId ID địa điểm cần kiểm tra
     * @return [Result.success] chứa true nếu đang yêu thích, false nếu không
     */
    suspend fun checkFavorite(token: String, placeId: String): Result<Boolean>
}
