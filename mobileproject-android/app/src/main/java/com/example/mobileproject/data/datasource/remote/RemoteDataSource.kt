package com.example.mobileproject.data.datasource.remote

import com.example.mobileproject.data.model.ProductDto
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    private val apiService: ApiService,
) {
    suspend fun getProducts(): List<ProductDto> = apiService.getProducts()
}
