package com.example.mobileproject.data.repository

import com.example.mobileproject.core.result.Resource
import com.example.mobileproject.data.datasource.remote.ApiService
import com.example.mobileproject.data.model.transaction.IncomeRequestDto
import com.example.mobileproject.data.model.transaction.TransactionDto
import com.example.mobileproject.data.model.transaction.TransactionRequestDto
import com.example.mobileproject.domain.entity.Transaction
import com.example.mobileproject.domain.entity.TransactionResponse
import com.example.mobileproject.domain.repository.TransactionRepository
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
) : TransactionRepository {

    override suspend fun createTransaction(
        coupleId: String,
        amount: Long,
        type: String,
        category: String,
        note: String?
    ): Resource<TransactionResponse> {
        return try {
            val response = apiService.createTransaction(
                TransactionRequestDto(
                    coupleId = coupleId,
                    amount = amount,
                    type = type,
                    category = category,
                    note = note
                )
            )
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Resource.Success(
                    TransactionResponse(
                        success = body.success,
                        message = body.message,
                        transactionId = body.transactionId,
                        amount = body.amount,
                        type = body.type,
                        category = body.category,
                        note = body.note,
                        totalBalance = body.totalBalance,
                        createdAt = body.createdAt
                    )
                )
            } else {
                Resource.Error(Exception(response.message()))
            }
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    override suspend fun processIncome(
        coupleId: String,
        amount: Long,
        targetType: String,
        goalId: String?,
        note: String?
    ): Resource<TransactionResponse> {
        return try {
            val response = apiService.processIncome(
                IncomeRequestDto(
                    coupleId = coupleId,
                    amount = amount,
                    targetType = targetType,
                    goalId = goalId,
                    note = note
                )
            )
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Resource.Success(
                    TransactionResponse(
                        success = body.success,
                        message = body.message,
                        transactionId = body.transactionId,
                        amount = body.amount,
                        type = body.type,
                        category = body.category,
                        note = body.note,
                        totalBalance = body.totalBalance,
                        createdAt = body.createdAt
                    )
                )
            } else {
                Resource.Error(Exception(response.message()))
            }
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    override suspend fun getTransactions(coupleId: String): Resource<List<Transaction>> {
        return try {
            val response = apiService.getTransactions(coupleId)
            if (response.isSuccessful && response.body() != null) {
                val body: List<TransactionDto> = response.body()!!
                val transactions = body.map { dto ->
                    Transaction(
                        id = dto.id,
                        coupleId = dto.coupleId,
                        amount = dto.amount,
                        type = com.example.mobileproject.domain.entity.TransactionType.valueOf(dto.type),
                        category = dto.category,
                        note = dto.note ?: "",
                        createdAt = dto.createdAt
                    )
                }
                Resource.Success(transactions)
            } else {
                Resource.Error(Exception(response.message()))
            }
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }
}