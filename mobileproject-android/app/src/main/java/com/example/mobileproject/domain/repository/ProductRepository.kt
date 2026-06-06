package com.example.mobileproject.domain.repository

import com.example.mobileproject.domain.entity.Product

/**
 * Repository truy vấn danh sách sản phẩm.
 *
 * Cung cấp dữ liệu sản phẩm cho các màn hình liên quan
 * đến danh mục hoặc cửa hàng trong ứng dụng.
 */
interface ProductRepository {
    /**
     * Lấy danh sách tất cả sản phẩm hiện có.
     *
     * @return Danh sách [Product], có thể rỗng nếu chưa có sản phẩm nào
     * @throws java.io.IOException khi mất kết nối mạng
     */
    suspend fun getProducts(): List<Product>
}
