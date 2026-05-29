package com.example.mobileproject.data.repository

import com.example.mobileproject.core.result.Resource
import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.data.datasource.remote.ApiService
import com.example.mobileproject.data.mapper.toDomain
import com.example.mobileproject.data.model.transaction.PayoutCreateRequestDto
import com.example.mobileproject.domain.entity.PayoutRequest
import com.example.mobileproject.domain.repository.PayoutRepository
import javax.inject.Inject

class PayoutRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val authSessionStore: AuthSessionStore,
) : PayoutRepository {

    private fun authorizationHeader(): String {
        val token = authSessionStore.load()?.token?.trim().orEmpty()
        if (token.isBlank()) throw IllegalStateException("Missing auth token")
        return "Bearer $token"
    }

    override suspend fun createPayout(coupleId: String, amount: Long): Resource<PayoutRequest> {
        return try {
            val response = apiService.createPayout(
                authorizationHeader(),
                PayoutCreateRequestDto(coupleId = coupleId, amount = amount),
            )
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.toDomain())
            } else {
                Resource.Error(Exception(response.errorBody()?.string() ?: response.message()))
            }
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    override suspend fun getPayout(id: String): Resource<PayoutRequest> {
        return try {
            val response = apiService.getPayout(authorizationHeader(), id)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.toDomain())
            } else {
                Resource.Error(Exception(response.errorBody()?.string() ?: response.message()))
            }
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }
}

