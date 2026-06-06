package com.example.mobileproject.data.repository

import com.example.mobileproject.data.datasource.remote.RemoteDataSource
import com.example.mobileproject.data.mapper.toDomain
import com.example.mobileproject.domain.entity.Product
import com.example.mobileproject.domain.repository.ProductRepository
import javax.inject.Inject

/**
 * Implementation của [ProductRepository].
 *
 * ## Caching strategy
 * Không có cache,每次都 gọi API trực tiếp (stateless).
 *
 * ## Data transformation
 * Chuyển đổi [ProductDto] -> [Product] (domain entity) qua mapper extension `toDomain()`.
 *
 * ## Threading
 * Hàm `suspend` chạy trên IO dispatcher (Retrofit default).
 */
class ProductRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
) : ProductRepository {

    /**
     * Lấy danh sách sản phẩm từ remote API.
     *
     * @return danh sách [Product] đã được map sang domain entity
     */
    override suspend fun getProducts(): List<Product> {
        return remoteDataSource.getProducts().map { it.toDomain() }
    }
}
