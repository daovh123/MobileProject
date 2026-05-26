package com.example.mobileproject.data.repository

import com.example.mobileproject.core.result.Resource
import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.data.datasource.remote.ApiService
import com.example.mobileproject.data.model.transaction.TopUpCreateRequestDto
import com.example.mobileproject.data.model.transaction.TopUpResponseDto
import com.example.mobileproject.domain.entity.AuthSession
import com.example.mobileproject.domain.entity.TopUpStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class TopUpRepositoryImplTest {

    private val apiService = mockk<ApiService>()
    private val authSessionStore = mockk<AuthSessionStore>()

    @Test
    fun `createTopUp sends auth header and maps response`() = runTest {
        every { authSessionStore.load() } returns authSession()
        coEvery {
            apiService.createTopUp("Bearer token-1", any<TopUpCreateRequestDto>())
        } returns Response.success(topUpDto(status = "PENDING"))

        val repository = TopUpRepositoryImpl(apiService, authSessionStore)
        val result = repository.createTopUp(
            coupleId = "couple-1",
            amount = 50_000L,
            bankId = "mbbank",
            bankName = "MB Bank",
            note = "demo",
        )

        assertTrue(result is Resource.Success)
        val topUp = (result as Resource.Success).data
        assertEquals("topup-1", topUp.id)
        assertEquals(TopUpStatus.PENDING, topUp.status)
        assertEquals("YMWABC123 demo", topUp.transferContent)

        coVerify {
            apiService.createTopUp(
                "Bearer token-1",
                TopUpCreateRequestDto(
                    coupleId = "couple-1",
                    amount = 50_000L,
                    bankId = "mbbank",
                    bankName = "MB Bank",
                    note = "demo",
                ),
            )
        }
    }

    @Test
    fun `getTopUp maps paid status and current balance`() = runTest {
        every { authSessionStore.load() } returns authSession()
        coEvery { apiService.getTopUp("Bearer token-1", "topup-1") } returns Response.success(
            topUpDto(status = "PAID", currentBalance = 170_000L),
        )

        val repository = TopUpRepositoryImpl(apiService, authSessionStore)
        val result = repository.getTopUp("topup-1")

        assertTrue(result is Resource.Success)
        val topUp = (result as Resource.Success).data
        assertEquals(TopUpStatus.PAID, topUp.status)
        assertEquals(170_000L, topUp.currentBalance)
    }

    private fun authSession() = AuthSession(
        token = "token-1",
        username = "alice",
        email = "alice@example.com",
        profileCompleted = true,
        coupleConnected = true,
        coupleId = "couple-1",
    )

    private fun topUpDto(
        status: String,
        currentBalance: Long? = 120_000L,
    ) = TopUpResponseDto(
        id = "topup-1",
        coupleId = "couple-1",
        amount = 50_000L,
        status = status,
        bankId = "mbbank",
        bankName = "MB Bank",
        accountNumber = "0123456789",
        accountName = "YOU AND ME WALLET",
        transferCode = "YMWABC123",
        transferContent = "YMWABC123 demo",
        qrContent = "BANK_TRANSFER|mbbank|0123456789|50000|YOU AND ME WALLET|YMWABC123 demo",
        qrImageUrl = "https://img.vietqr.io/image/mbbank-0123456789-compact2.png",
        note = "demo",
        createdAt = "2026-05-25T00:00:00Z",
        paidAt = if (status == "PAID") "2026-05-25T00:01:00Z" else null,
        currentBalance = currentBalance,
    )
}
