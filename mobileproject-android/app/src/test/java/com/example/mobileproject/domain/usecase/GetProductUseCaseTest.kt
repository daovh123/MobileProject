package com.example.mobileproject.domain.usecase

import com.example.mobileproject.domain.entity.Product
import com.example.mobileproject.domain.repository.ProductRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class GetProductUseCaseTest {

    @Test
    fun `invoke returns data from repository`() = runBlocking {
        val expected = listOf(
            Product(id = "1", name = "Coffee"),
            Product(id = "2", name = "Tea"),
        )

        val repository = object : ProductRepository {
            override suspend fun getProducts(): List<Product> = expected
        }

        val useCase = GetProductUseCase(repository)

        assertEquals(expected, useCase())
    }
}
