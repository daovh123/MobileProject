package com.example.mobileproject.domain.repository

import com.example.mobileproject.domain.entity.Place

interface FavoriteRepository {
    suspend fun toggleFavorite(token: String, placeId: String): Result<Boolean>
    suspend fun getFavorites(token: String): Result<List<Place>>
    suspend fun checkFavorite(token: String, placeId: String): Result<Boolean>
}
