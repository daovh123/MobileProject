package com.example.mobileproject.domain.model

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

/**
 * Phân loại mục tiêu tiết kiệm (saving goals) của cặp đôi.
 *
 * Mỗi danh mục đại diện cho một động lực tiết kiệm khác nhau,
 * giúp cặp đôi tổ chức và theo dõi tiến độ tài chính chung
 * theo từng mục đích cụ thể.
 *
 * @property id Mã định danh duy nhất dùng để lưu trữ và đồng bộ với backend
 * @property displayName Tên hiển thị tiếng Việt trên UI
 * @property icon Icon đại diện hiển thị trên giao diện Compose
 */
sealed class GoalCategory(
    val id: String,
    val displayName: String,
    val icon: ImageVector
) {
    /** Du lịch: tiết kiệm cho chuyến đi chơi, honeymoon, vacation */
    object Travel : GoalCategory("TRAVEL", "Du lịch", Icons.Default.Flight)
    /** Công nghệ: tiết kiệm mua điện thoại, laptop, thiết bị điện tử */
    object Tech : GoalCategory("TECH", "Công nghệ", Icons.Default.Devices)
    /** Nhà cửa: tiết kiệm mua nhà, sửa nhà, mua nội thất */
    object Home : GoalCategory("HOME", "Nhà cửa", Icons.Default.Home)
    /** Tiết kiệm chung: dự phòng tài chính, tích lũy dài hạn */
    object Savings : GoalCategory("TIẾT KIỆM", "Tiết kiệm", Icons.Default.Savings)
    /** Khác: các mục tiêu không thuộc danh mục phía trên */
    object Others : GoalCategory("OTHERS", "Khác", Icons.Default.Star)

    companion object {
        /**
         * Trả về danh sách tất cả các danh mục mục tiêu.
         */
        fun getAll(): List<GoalCategory> = listOf(
            Travel, Tech, Home, Savings, Others
        )
        
        /**
         * Tìm danh mục theo [id], trả về [Others] nếu không tìm thấy
         * để tránh crash khi dữ liệu cũ hoặc đồng bộ có category không hợp lệ.
         */
        fun fromId(id: String): GoalCategory = getAll().find { it.id == id } ?: Others
    }
}

