package com.example.mobileproject.domain.repository

import com.example.mobileproject.domain.entity.Place

interface HistoryRepository {
    suspend fun recordView(token: String, placeId: String): Result<Unit>
    suspend fun getHistory(token: String): Result<List<Place>>
    suspend fun clearHistory(token: String): Result<Unit>
}
