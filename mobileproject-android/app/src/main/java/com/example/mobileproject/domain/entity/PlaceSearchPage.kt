package com.example.mobileproject.domain.entity

/**
 * Domain entity đại diện cho một trang kết quả tìm kiếm địa điểm.
 * Hỗ trợ phân trang (pagination) khi người dùng cuộn qua danh sách địa điểm.
 *
 * @property items Danh sách địa điểm trong trang hiện tại
 * @property total Tổng số địa điểm khớp với điều kiện tìm kiếm, dùng để tính tổng số trang
 * @property page Số trang hiện tại (0-indexed)
 * @property size Số lượng mục trên mỗi trang, phục vụ logic tải thêm (load more)
 */
data class PlaceSearchPage(
    val items: List<Place> = emptyList(),
    val total: Long = 0,
    val page: Int = 0,
    val size: Int = 0,
)
