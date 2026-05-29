package com.example.mobileproject.domain.repository

import com.example.mobileproject.core.result.Resource
import com.example.mobileproject.domain.entity.PayoutRequest

interface PayoutRepository {
    suspend fun createPayout(coupleId: String, amount: Long): Resource<PayoutRequest>
    suspend fun getPayout(id: String): Resource<PayoutRequest>
}

