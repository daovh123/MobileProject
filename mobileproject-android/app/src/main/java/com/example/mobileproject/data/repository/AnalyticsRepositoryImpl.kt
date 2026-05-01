package com.example.mobileproject.data.repository

import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.data.datasource.remote.ApiService
import com.example.mobileproject.data.mapper.toEntity
import com.example.mobileproject.domain.entity.CategoryBreakdown
import com.example.mobileproject.domain.entity.SpendingTrend
import com.example.mobileproject.domain.repository.AnalyticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AnalyticsRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val authSessionStore: AuthSessionStore
) : AnalyticsRepository {

    private fun authorizationHeader(): String {
        val token = authSessionStore.load()?.token.orEmpty()
        return "Bearer $token"
    }

    override suspend fun getCategoryBreakdown(
        coupleId: String,
        month: Int,
        year: Int
    ): Flow<Result<List<CategoryBreakdown>>> = flow {
        try {
            val response = apiService.getCategoryBreakdown(authorizationHeader(), coupleId, month, year)
            if (response.isSuccessful && response.body() != null) {
                val dtos = response.body()!!
                val totalSum = dtos.sumOf { it.totalAmount }
                val entities = dtos.map { it.toEntity(totalSum) }
                emit(Result.success(entities))
            } else {
                emit(Result.failure(Exception(response.message())))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override suspend fun getSpendingTrend(
        coupleId: String,
        month: Int,
        year: Int
    ): Flow<Result<List<SpendingTrend>>> = flow {
        try {
            // Sửa lại để gọi getMonthlyTrend chỉ với year theo API mới
            val response = apiService.getMonthlyTrend(authorizationHeader(), coupleId, year)
            if (response.isSuccessful && response.body() != null) {
                val dtos = response.body()!!
                val entities = dtos.map { it.toEntity() }
                emit(Result.success(entities))
            } else {
                emit(Result.failure(Exception(response.message())))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
