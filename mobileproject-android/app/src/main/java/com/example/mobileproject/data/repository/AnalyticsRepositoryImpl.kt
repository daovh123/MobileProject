package com.example.mobileproject.data.repository

import com.example.mobileproject.core.result.Resource
import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.data.datasource.remote.ApiService
import com.example.mobileproject.domain.entity.CategoryBreakdown
import com.example.mobileproject.domain.entity.SpendingTrend
import com.example.mobileproject.domain.repository.AnalyticsRepository
import javax.inject.Inject

class AnalyticsRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val authSessionStore: AuthSessionStore,
) : AnalyticsRepository {

    private fun authorizationHeader(): String {
        val token = authSessionStore.load()?.token?.trim().orEmpty()
        if (token.isBlank()) throw IllegalStateException("Missing auth token")
        return "Bearer $token"
    }

    override suspend fun getCategoryBreakdown(
        coupleId: String,
        startDate: String,
        endDate: String
    ): Resource<List<CategoryBreakdown>> {
        return try {
            val response = apiService.getCategoryBreakdown(authorizationHeader(), coupleId, startDate, endDate)
            if (response.isSuccessful && response.body() != null) {
                val breakdown = response.body()!!.map { dto ->
                    CategoryBreakdown(
                        category = dto.category ?: "UNKNOWN",
                        totalAmount = dto.totalAmount ?: 0L
                    )
                }
                Resource.Success(breakdown)
            } else {
                Resource.Error(Exception(response.message()))
            }
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    override suspend fun getSpendingTrend(
        coupleId: String,
        year: Int,
        month: Int
    ): Resource<List<SpendingTrend>> {
        return try {
            val response = apiService.getSpendingTrend(authorizationHeader(), coupleId, year, month)
            if (response.isSuccessful && response.body() != null) {
                val trend = response.body()!!.map { dto ->
                    SpendingTrend(
                        date = dto.date,
                        totalAmount = dto.totalAmount ?: 0L
                    )
                }
                Resource.Success(trend)
            } else {
                Resource.Error(Exception(response.message()))
            }
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }
}