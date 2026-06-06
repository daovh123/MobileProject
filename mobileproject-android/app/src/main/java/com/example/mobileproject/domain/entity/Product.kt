package com.example.mobileproject.domain.entity

/**
 * Domain entity representing a product in the system.
 * Used as a lightweight reference object for product catalog or shopping features.
 *
 * @property id Định danh duy nhất của sản phẩm, dùng để truy vấn và liên kết dữ liệu
 * @property name Tên hiển thị của sản phẩm, có thể dùng cho tìm kiếm và hiển thị UI
 */
data class Product(
    val id: String,
    val name: String,
)
