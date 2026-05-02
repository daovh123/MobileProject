package com.example.mobileproject.presentation.viewmodel

import com.example.mobileproject.core.result.Resource
import com.example.mobileproject.domain.entity.ContributeResponse
import com.example.mobileproject.domain.entity.GoalResponse
import com.example.mobileproject.domain.entity.GoalStatus
import com.example.mobileproject.domain.entity.SavingGoal
import com.example.mobileproject.domain.repository.GoalRepository
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
class GoalViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `loadGoals success populates goals list`() = runTest {
        val goal1 = SavingGoal(
            id = "g1",
            coupleId = "couple-1",
            name = "Vacation",
            targetAmount = 5000000L,
            currentAmount = 1000000L,
            status = GoalStatus.IN_PROGRESS,
            deadline = "2024-12-31",
            createdAt = "2024-01-01"
        )
        val goal2 = SavingGoal(
            id = "g2",
            coupleId = "couple-1",
            name = "New Car",
            targetAmount = 20000000L,
            currentAmount = 500000L,
            status = GoalStatus.IN_PROGRESS,
            deadline = null,
            createdAt = "2024-01-02"
        )
        val repository = FakeGoalRepository().apply {
            getGoalsResult = Resource.Success(listOf(goal1, goal2))
        }
        val viewModel = GoalViewModel(repository)

        viewModel.loadGoals("couple-1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.goals.size)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `loadGoals error sets error and keeps goals empty`() = runTest {
        val repository = FakeGoalRepository().apply {
            getGoalsResult = Resource.Error(RuntimeException("Network error"))
        }
        val viewModel = GoalViewModel(repository)

        viewModel.loadGoals("couple-1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Network error", state.error)
        assertTrue(state.goals.isEmpty())
        assertFalse(state.isLoading)
    }

    @Test
    fun `createGoal success sets createGoalResponse`() = runTest {
        val response = GoalResponse(
            success = true,
            message = "Goal created",
            goalId = "g3",
            name = "Emergency Fund",
            targetAmount = 10000000L,
            currentAmount = 0L,
            status = GoalStatus.IN_PROGRESS,
            deadline = null,
            createdAt = "2024-01-03"
        )
        val repository = FakeGoalRepository().apply {
            createGoalResult = Resource.Success(response)
        }
        val viewModel = GoalViewModel(repository)

        viewModel.createGoal("couple-1", "Emergency Fund", 10000000L, null)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.createGoalResponse)
        assertEquals("Goal created", state.createGoalResponse?.message)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `createGoal error sets error message`() = runTest {
        val repository = FakeGoalRepository().apply {
            createGoalResult = Resource.Error(RuntimeException("Goal creation failed"))
        }
        val viewModel = GoalViewModel(repository)

        viewModel.createGoal("couple-1", "Emergency Fund", 10000000L, null)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.error)
        assertNull(state.createGoalResponse)
        assertFalse(state.isLoading)
    }

    @Test
    fun `clearState resets to default`() = runTest {
        val goal = SavingGoal(
            id = "g1",
            coupleId = "couple-1",
            name = "Vacation",
            targetAmount = 5000000L,
            currentAmount = 1000000L,
            status = GoalStatus.IN_PROGRESS,
            deadline = null,
            createdAt = null
        )
        val repository = FakeGoalRepository().apply {
            getGoalsResult = Resource.Success(listOf(goal))
        }
        val viewModel = GoalViewModel(repository)

        viewModel.loadGoals("couple-1")
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.goals.size)

        viewModel.clearState()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.goals.isEmpty())
        assertNull(state.createGoalResponse)
        assertNull(state.contributeResponse)
        assertNull(state.error)
    }

    private class FakeGoalRepository : GoalRepository {

        var createGoalResult: Resource<GoalResponse> =
            Resource.Error(RuntimeException("not configured"))
        var getGoalsResult: Resource<List<SavingGoal>> =
            Resource.Error(RuntimeException("not configured"))

        override suspend fun createGoal(
            coupleId: String,
            name: String,
            targetAmount: Long,
            deadline: String?
        ): Resource<GoalResponse> = createGoalResult

        override suspend fun getGoalsByCouple(coupleId: String): Resource<List<SavingGoal>> =
            getGoalsResult

        override suspend fun contributeFromWallet(
            goalId: String,
            amount: Long,
            contributorId: String,
            note: String?
        ): Resource<ContributeResponse> = error("not used")

        override suspend fun contributeToGoal(
            goalId: String,
            amount: Long,
            contributorId: String,
            note: String?
        ): Resource<ContributeResponse> = error("not used")
    }
}
