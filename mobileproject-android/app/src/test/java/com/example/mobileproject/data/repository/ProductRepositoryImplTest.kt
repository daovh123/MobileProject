package com.example.mobileproject.data.repository

import com.example.mobileproject.data.datasource.remote.ApiService
import com.example.mobileproject.data.datasource.remote.RemoteDataSource
import com.example.mobileproject.data.model.ProductDto
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ProductRepositoryImplTest {

    @Test
    fun `getProducts maps dto to domain`() = runBlocking {
        val apiService = object : ApiService {
            override suspend fun getProducts(): List<ProductDto> = listOf(
                ProductDto(id = "1", name = "Coffee"),
                ProductDto(id = "2", name = "Tea"),
            )
        }

        val remoteDataSource = RemoteDataSource(apiService)
        val repository = ProductRepositoryImpl(remoteDataSource)

        val result = repository.getProducts()

        assertEquals(listOf("1", "2"), result.map { it.id })
        assertEquals(listOf("Coffee", "Tea"), result.map { it.name })
    }
}
