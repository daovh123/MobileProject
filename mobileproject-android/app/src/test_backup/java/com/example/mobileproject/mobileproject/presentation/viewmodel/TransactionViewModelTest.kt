package com.example.mobileproject.presentation.viewmodel

import com.example.mobileproject.core.result.Resource
import com.example.mobileproject.domain.entity.Transaction
import com.example.mobileproject.domain.entity.TransactionResponse
import com.example.mobileproject.domain.entity.TransactionType
import com.example.mobileproject.domain.repository.TransactionRepository
import com.example.mobileproject.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `loadTransactions success updates state transactions`() = runTest {
        val tx1 = Transaction(
            id = "1",
            coupleId = "couple-1",
            amount = 100000L,
            type = TransactionType.EXPENSE,
            category = "food",
            note = "lunch",
            createdAt = "2024-01-01"
        )
        val tx2 = Transaction(
            id = "2",
            coupleId = "couple-1",
            amount = 50000L,
            type = TransactionType.INCOME,
            category = "salary",
            note = "monthly",
            createdAt = "2024-01-02"
        )
        val repository = FakeTransactionRepository().apply {
            getTransactionsResult = Resource.Success(listOf(tx1, tx2))
        }
        val viewModel = TransactionViewModel(repository)

        viewModel.loadTransactions("couple-1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.transactions.size)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `loadTransactions error sets error state`() = runTest {
        val repository = FakeTransactionRepository().apply {
            getTransactionsResult = Resource.Error(RuntimeException("Network error"))
        }
        val viewModel = TransactionViewModel(repository)

        viewModel.loadTransactions("couple-1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Network error", state.error)
        assertTrue(state.transactions.isEmpty())
        assertFalse(state.isLoading)
    }

    @Test
    fun `createTransaction success updates transactionResponse and totalBalance`() = runTest {
        val response = TransactionResponse(
            success = true,
            message = "ok",
            transactionId = "t1",
            amount = 50000L,
            type = "EXPENSE",
            category = "food",
            note = null,
            totalBalance = 200000L,
            createdAt = null
        )
        val repository = FakeTransactionRepository().apply {
            createTransactionResult = Resource.Success(response)
        }
        val viewModel = TransactionViewModel(repository)

        viewModel.createTransaction("couple-1", 50000L, "EXPENSE", "food", null)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(200000L, state.totalBalance)
        assertNotNull(state.transactionResponse)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `createTransaction error sets error message`() = runTest {
        val repository = FakeTransactionRepository().apply {
            createTransactionResult = Resource.Error(RuntimeException("Transaction failed"))
        }
        val viewModel = TransactionViewModel(repository)

        viewModel.createTransaction("couple-1", 50000L, "EXPENSE", "food", null)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.error)
        assertFalse(state.isLoading)
        assertNull(state.transactionResponse)
    }

    private class FakeTransactionRepository : TransactionRepository {

        var createTransactionResult: Resource<TransactionResponse> =
            Resource.Error(RuntimeException("not configured"))
        var getTransactionsResult: Resource<List<Transaction>> =
            Resource.Error(RuntimeException("not configured"))

        override suspend fun createTransaction(
            coupleId: String,
            amount: Long,
            type: String,
            category: String,
            note: String?
        ): Resource<TransactionResponse> = createTransactionResult

        override suspend fun processIncome(
            coupleId: String,
            amount: Long,
            targetType: String,
            goalId: String?,
            note: String?
        ): Resource<TransactionResponse> = error("not used")

        override suspend fun getTransactions(coupleId: String): Resource<List<Transaction>> =
            getTransactionsResult
    }
}
