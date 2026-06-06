package com.example.mobileproject.domain.entity

/**
 * Domain entity chứa các tuỳ chọn lọc (filter) cho tính năng tìm kiếm địa điểm.
 * Được sử dụng để populate bộ lọc UI, cho phép người dùng thu hẹp kết quả theo khu vực.
 *
 * @property districts Danh sách quận/huyện có sẵn để lọc, lấy từ dữ liệu địa điểm hiện có
 * @property provinces Danh sách tỉnh/thành phố có sẵn để lọc, thường dùng cho phạm vi rộng hơn
 */
data class PlaceFilterOptions(
    val districts: List<String> = emptyList(),
    val provinces: List<String> = emptyList(),
)
