package com.example.mobileproject.data.repository

import com.example.mobileproject.data.datasource.remote.RemoteDataSource
import com.example.mobileproject.data.mapper.toDomain
import com.example.mobileproject.domain.entity.Product
import com.example.mobileproject.domain.repository.ProductRepository
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
) : ProductRepository {

    override suspend fun getProducts(): List<Product> {
        return remoteDataSource.getProducts().map { it.toDomain() }
    }
}
