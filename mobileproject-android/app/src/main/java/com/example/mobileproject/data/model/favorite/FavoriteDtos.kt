package com.example.mobileproject.data.model.favorite

import com.example.mobileproject.data.model.place.PlaceDto

/**
 * Response từ API toggle yêu thích địa điểm (thêm/bỏ yêu thích).
 *
 * Hành động toggle: nếu địa điểm chưa yêu thích → thêm, đã yêu thích → bỏ.
 * [favoriteId] chỉ có giá trị khi vừa thêm yêu thích, null khi vừa bỏ yêu thích.
 */
data class FavoriteToggleResponseDto(
    /** true nếu thao tác toggle thành công. */
    val success: Boolean,
    /** Thông báo mô tả kết quả. */
    val message: String,
    /** UUID của bản ghi yêu thích, chỉ có khi vừa thêm, null khi vừa bỏ. */
    val favoriteId: String?,
    /** UUID của địa điểm đã toggle, nullable. */
    val placeId: String?,
    /** Thời điểm thêm yêu thích, định dạng ISO datetime, nullable. */
    val createdAt: String?,
)

/**
 * Response từ API lấy danh sách địa điểm yêu thích.
 *
 * [places] có thể rỗng nếu người dùng chưa có yêu thích nào.
 */
data class FavoriteListResponseDto(
    /** true nếu API thành công. */
    val success: Boolean,
    /** Thông báo mô tả kết quả. */
    val message: String,
    /** Danh sách địa điểm yêu thích, có thể rỗng. */
    val places: List<PlaceDto>,
)

/**
 * Response từ API lấy lịch sử địa điểm đã đi.
 *
 * [places] có thể rỗng nếu người dùng chưa ghi nhận địa điểm nào.
 */
data class HistoryListResponseDto(
    /** true nếu API thành công. */
    val success: Boolean,
    /** Thông báo mô tả kết quả. */
    val message: String,
    /** Danh sách địa điểm trong lịch sử, có thể rỗng. */
    val places: List<PlaceDto>,
)
