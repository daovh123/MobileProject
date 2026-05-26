package com.example.mobileproject.domain.repository

import com.example.mobileproject.core.result.Resource
import com.example.mobileproject.domain.entity.Transaction
import com.example.mobileproject.domain.entity.TransactionResponse

interface TransactionRepository {
    suspend fun createTransaction(
        coupleId: String,
        amount: Long,
        type: String,
        category: String,
        note: String?
    ): Resource<TransactionResponse>

    suspend fun processIncome(
        coupleId: String,
        amount: Long,
        targetType: String,
        goalId: String?,
        note: String?
    ): Resource<TransactionResponse>

    suspend fun getTransactions(coupleId: String): Resource<List<Transaction>>
}
