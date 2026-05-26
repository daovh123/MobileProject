package com.example.mobileproject.data.repository

import com.example.mobileproject.core.result.Resource
import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.data.datasource.remote.ApiService
import com.example.mobileproject.data.mapper.toDomain
import com.example.mobileproject.data.model.transaction.TopUpCreateRequestDto
import com.example.mobileproject.domain.entity.TopUpRequest
import com.example.mobileproject.domain.repository.TopUpRepository
import javax.inject.Inject

class TopUpRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val authSessionStore: AuthSessionStore,
) : TopUpRepository {

    private fun authorizationHeader(): String {
        val token = authSessionStore.load()?.token?.trim().orEmpty()
        if (token.isBlank()) throw IllegalStateException("Missing auth token")
        return "Bearer $token"
    }

    override suspend fun createTopUp(
        coupleId: String,
        amount: Long,
        bankId: String,
        bankName: String,
        note: String?,
    ): Resource<TopUpRequest> {
        return try {
            val response = apiService.createTopUp(
                authorizationHeader(),
                TopUpCreateRequestDto(
                    coupleId = coupleId,
                    amount = amount,
                    bankId = bankId,
                    bankName = bankName,
                    note = note,
                ),
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

    override suspend fun getTopUp(id: String): Resource<TopUpRequest> {
        return try {
            val response = apiService.getTopUp(authorizationHeader(), id)
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
