package com.example.mobileproject.domain.repository

import com.example.mobileproject.domain.entity.Product

interface ProductRepository {
    suspend fun getProducts(): List<Product>
}
