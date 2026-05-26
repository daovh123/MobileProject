package com.example.mobileproject.domain.repository

import com.example.mobileproject.core.result.Resource
import com.example.mobileproject.domain.entity.TopUpRequest

interface TopUpRepository {
    suspend fun createTopUp(
        coupleId: String,
        amount: Long,
        bankId: String,
        bankName: String,
        note: String?,
    ): Resource<TopUpRequest>

    suspend fun getTopUp(id: String): Resource<TopUpRequest>
}
