package com.example.mobileproject.presentation.viewmodel

import com.example.mobileproject.core.result.Resource
import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.domain.entity.AuthSession
import com.example.mobileproject.domain.entity.TopUpRequest
import com.example.mobileproject.domain.entity.TopUpStatus
import com.example.mobileproject.domain.entity.Wallet
import com.example.mobileproject.domain.repository.TopUpRepository
import com.example.mobileproject.domain.repository.WalletRepository
import com.example.mobileproject.testutil.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TopUpViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authSessionStore = mockk<AuthSessionStore>()

    @Test
    fun `createTopUpRequest creates pending request and opens qr without marking success`() = runTest {
        every { authSessionStore.load() } returns authSession()
        val topUpRepository = FakeTopUpRepository()
        val walletRepository = FakeWalletRepository(Wallet("couple-1", "Vi chung", 120_000L))
        val viewModel = TopUpViewModel(topUpRepository, walletRepository, authSessionStore)
        val event = async { viewModel.navigationEvents.first() }

        viewModel.onAmountChange("50000")
        viewModel.onNoteChange("demo")
        viewModel.onBankSelected(TopUpViewModel.findBankById("mbbank")!!)
        viewModel.createTopUpRequest(TopUpPaymentMode.QR)
        advanceUntilIdle()

        assertEquals("couple-1", topUpRepository.createdCoupleId)
        assertEquals(50_000L, topUpRepository.createdAmount)
        assertEquals("mbbank", topUpRepository.createdBankId)
        assertEquals("demo", topUpRepository.createdNote)
        assertEquals(TopUpStatus.PENDING, viewModel.uiState.value.activeTopUp?.status)
        assertFalse(viewModel.uiState.value.isSuccess)
        assertEquals(TopUpNavigationEvent.OpenQr("topup-1"), event.await())
    }

    @Test
    fun `polling updates wallet only after backend marks top up paid`() = runTest {
        every { authSessionStore.load() } returns authSession()
        val topUpRepository = FakeTopUpRepository(
            getTopUpResults = ArrayDeque(
                listOf(
                    topUp(status = TopUpStatus.PENDING, currentBalance = 120_000L),
                    topUp(status = TopUpStatus.PAID, currentBalance = 170_000L),
                ),
            ),
        )
        val walletRepository = FakeWalletRepository(Wallet("couple-1", "Vi chung", 120_000L))
        val viewModel = TopUpViewModel(topUpRepository, walletRepository, authSessionStore)

        viewModel.startTopUpStatusPolling("topup-1")
        runCurrent()
        assertTrue(viewModel.uiState.value.isPolling)
        assertEquals(emptyList<Long>(), walletRepository.localBalanceUpdates)

        advanceTimeBy(3_000L)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isPolling)
        assertTrue(viewModel.uiState.value.isSuccess)
        assertEquals(170_000L, viewModel.uiState.value.currentBalance)
        assertEquals(listOf(170_000L), walletRepository.localBalanceUpdates)
    }

    private fun authSession() = AuthSession(
        token = "token-1",
        username = "alice",
        email = "alice@example.com",
        profileCompleted = true,
        coupleConnected = true,
        coupleId = "couple-1",
    )

    private class FakeTopUpRepository(
        private val createResult: TopUpRequest = topUp(),
        private val getTopUpResults: ArrayDeque<TopUpRequest> = ArrayDeque(),
    ) : TopUpRepository {
        var createdCoupleId: String? = null
        var createdAmount: Long? = null
        var createdBankId: String? = null
        var createdNote: String? = null

        override suspend fun createTopUp(
            coupleId: String,
            amount: Long,
            bankId: String,
            bankName: String,
            note: String?,
        ): Resource<TopUpRequest> {
            createdCoupleId = coupleId
            createdAmount = amount
            createdBankId = bankId
            createdNote = note
            return Resource.Success(createResult)
        }

        override suspend fun getTopUp(id: String): Resource<TopUpRequest> {
            return Resource.Success(getTopUpResults.removeFirstOrNull() ?: createResult)
        }
    }

    private class FakeWalletRepository(wallet: Wallet) : WalletRepository {
        private val walletStateFlow = MutableStateFlow<Wallet?>(wallet)
        val localBalanceUpdates = mutableListOf<Long>()

        override val walletState: Flow<Wallet?> = walletStateFlow

        override fun getWallet(coupleId: String, token: String): Flow<Result<Wallet>> {
            return flowOf(Result.success(walletStateFlow.value!!))
        }

        override suspend fun updateLocalBalance(newBalance: Long) {
            localBalanceUpdates += newBalance
            walletStateFlow.value = walletStateFlow.value?.copy(balance = newBalance)
        }
    }

    private companion object {
        fun topUp(
            status: TopUpStatus = TopUpStatus.PENDING,
            currentBalance: Long? = 120_000L,
        ) = TopUpRequest(
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
            createdAt = "2026-05-25T00:00:00Z",
            paidAt = if (status == TopUpStatus.PAID) "2026-05-25T00:01:00Z" else null,
            currentBalance = currentBalance,
        )
    }
}
