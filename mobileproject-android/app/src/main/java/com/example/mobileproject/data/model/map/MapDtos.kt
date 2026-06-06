package com.example.mobileproject.data.model.map

/**
 * DTO đại diện cho vị trí địa lý của một người dùng.
 *
 * Dùng trên bản đồ couple để hiển thị vị trí của cả hai người.
 * [updatedAt] cho biết thời điểm cập nhật vị trí lần cuối.
 */
data class LocationDto(
    /** Vĩ độ (latitude), nullable nếu chưa có dữ liệu. */
    val latitude: Double?,
    /** Kinh độ (longitude), nullable nếu chưa có dữ liệu. */
    val longitude: Double?,
    /** Thời điểm cập nhật vị trí, định dạng ISO datetime, nullable. */
    val updatedAt: String?,
)

/**
 * Response từ API lấy vị trí cuối cùng của cả hai thành viên couple.
 *
 * Dùng để hiển thị cả hai vị trí trên bản đồ couple.
 * Vị trí có thể null nếu người dùng chưa bật chia sẻ vị trí.
 */
data class MapLastLocationsResponseDto(
    /** true nếu API thành công. */
    val success: Boolean,
    /** Thông báo mô tả kết quả. */
    val message: String,
    /** UUID của couple, nullable. */
    val coupleId: String?,
    /** Vị trí của người dùng hiện tại, nullable nếu chưa có dữ liệu. */
    val myLocation: LocationDto?,
    /** Vị trí của partner, nullable nếu partner chưa bật chia sẻ vị trí. */
    val partnerLocation: LocationDto?,
)
