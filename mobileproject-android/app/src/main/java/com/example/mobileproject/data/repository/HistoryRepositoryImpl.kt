package com.example.mobileproject.data.repository

import com.example.mobileproject.data.datasource.remote.ApiService
import com.example.mobileproject.data.mapper.toDomain
import com.example.mobileproject.domain.entity.Place
import com.example.mobileproject.domain.repository.HistoryRepository
import javax.inject.Inject

class HistoryRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
) : HistoryRepository {

    override suspend fun recordView(token: String, placeId: String): Result<Unit> {
        return runCatching {
            val response = apiService.recordHistory(authorizationHeader(token), placeId)
            if (!response.isSuccessful) {
                throw IllegalStateException("Record history failed")
            }
        }
    }

    override suspend fun getHistory(token: String): Result<List<Place>> {
        return runCatching {
            val response = apiService.getHistory(authorizationHeader(token))
            val body = response.body()

            if (!response.isSuccessful || body == null || !body.success) {
                throw IllegalStateException(body?.message ?: "Get history failed")
            }

            body.places.map { it.toDomain() }
        }
    }

    override suspend fun clearHistory(token: String): Result<Unit> {
        return runCatching {
            val response = apiService.clearHistory(authorizationHeader(token))
            if (!response.isSuccessful) {
                throw IllegalStateException("Clear history failed")
            }
        }
    }

    private fun authorizationHeader(token: String): String = "Bearer ${token.trim()}"
}
