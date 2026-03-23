package com.example.mobileproject.data.datasource.remote

import com.example.mobileproject.data.model.ProductDto
import retrofit2.http.GET

interface ApiService {

    @GET("products")
    suspend fun getProducts(): List<ProductDto>
}
