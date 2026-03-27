package com.example.mobileproject.domain.usecase

import com.example.mobileproject.domain.entity.Product
import com.example.mobileproject.domain.repository.ProductRepository
import javax.inject.Inject

class GetProductUseCase @Inject constructor(
    private val repository: ProductRepository,
) {
    suspend operator fun invoke(): List<Product> = repository.getProducts()
}
