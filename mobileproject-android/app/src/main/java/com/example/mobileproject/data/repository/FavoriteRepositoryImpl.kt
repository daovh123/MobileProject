package com.example.mobileproject.data.repository

import com.example.mobileproject.data.datasource.remote.ApiService
import com.example.mobileproject.data.mapper.toDomain
import com.example.mobileproject.domain.entity.Place
import com.example.mobileproject.domain.repository.FavoriteRepository
import javax.inject.Inject

class FavoriteRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
) : FavoriteRepository {

    override suspend fun toggleFavorite(token: String, placeId: String): Result<Boolean> {
        return runCatching {
            val response = apiService.toggleFavorite(authorizationHeader(token), placeId)
            val body = response.body()

            if (!response.isSuccessful || body == null || !body.success) {
                throw IllegalStateException(body?.message ?: "Toggle favorite failed")
            }

            body.message.contains("Added", ignoreCase = true)
        }
    }

    override suspend fun getFavorites(token: String): Result<List<Place>> {
        return runCatching {
            val response = apiService.getFavorites(authorizationHeader(token))
            val body = response.body()

            if (!response.isSuccessful || body == null || !body.success) {
                throw IllegalStateException(body?.message ?: "Get favorites failed")
            }

            body.places.map { it.toDomain() }
        }
    }

    override suspend fun checkFavorite(token: String, placeId: String): Result<Boolean> {
        return runCatching {
            val response = apiService.checkFavorite(authorizationHeader(token), placeId)
            val body = response.body() ?: throw IllegalStateException("Check favorite failed")

            if (!response.isSuccessful) {
                throw IllegalStateException(body.message)
            }

            body.success
        }
    }

    private fun authorizationHeader(token: String): String = "Bearer ${token.trim()}"
}
