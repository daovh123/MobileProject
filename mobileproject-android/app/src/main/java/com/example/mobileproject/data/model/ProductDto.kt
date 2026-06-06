package com.example.mobileproject.data.model

/**
 * DTO đại diện cho sản phẩm từ API.
 *
 * Ánh xạ trực tiếp từ JSON response, dùng để truyền dữ liệu sản phẩm
 * từ tầng data sang tầng domain thông qua [ProductMapper].
 */
data class ProductDto(
    /** UUID định danh duy nhất của sản phẩm, bắt buộc từ API. */
    val id: String,
    /** Tên hiển thị của sản phẩm, bắt buộc từ API. */
    val name: String,
)
