package com.example.mobileproject.data.datasource.remote

import com.example.mobileproject.data.model.ProductDto
import javax.inject.Inject

/**
 * Wrapper trung gian cho [ApiService], đóng vai trò abstraction layer.
 *
 * ## Tại sao cần wrapper này?
 * - **Giảm coupling**: Repository không phụ thuộc trực tiếp vào Retrofit interface
 * - **Dễ test**: Có thể mock [RemoteDataSource] thay vì mock toàn bộ [ApiService]
 * - **Centralize logic**: Có thể thêm retry, logging, caching ở tầng này
 *
 * Hiện tại chỉ wrap endpoint `getProducts()`, các endpoint khác được gọi trực tiếp từ Repository.
 * Có thể mở rộng wrapper này khi cần thêm cross-cutting concerns.
 */
class RemoteDataSource @Inject constructor(
    private val apiService: ApiService,
) {
    /**
     * Lấy danh sách sản phẩm từ API.
     *
     * @return danh sách [ProductDto]
     */
    suspend fun getProducts(): List<ProductDto> = apiService.getProducts()
}
