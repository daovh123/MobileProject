package com.example.mobileproject.presentation.viewmodel

import com.example.mobileproject.domain.entity.CoupleRequestAction
import com.example.mobileproject.domain.entity.CoupleStatus
import com.example.mobileproject.domain.entity.ProfileResult
import com.example.mobileproject.domain.repository.OnboardingRepository
import com.example.mobileproject.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `saveProfile with blank fields sets validation error`() {
        val viewModel = ProfileViewModel(FakeOnboardingRepository())

        viewModel.saveProfile(
            token = "token-1",
            fullName = "",
            nickName = null,
            birthDate = "",
            gender = "",
        )

        val state = viewModel.uiState.value
        assertEquals("Vui long nhap day du thong tin", state.errorMessage)
        assertFalse(state.isLoading)
        assertNull(state.savedProfile)
    }

    @Test
    fun `saveProfile success exposes saved profile`() = runTest {
        val repository = FakeOnboardingRepository().apply {
            saveProfileResult = Result.success(
                ProfileResult(
                    username = "alice",
                    fullName = "Alice",
                    nickName = "Ali",
                    birthDate = "2020-02-01",
                    gender = "FEMALE",
                    profileCompleted = true,
                    coupleConnected = false,
                )
            )
        }
        val viewModel = ProfileViewModel(repository)

        viewModel.saveProfile("token-1", "Alice", "Ali", "2020-02-01", "FEMALE")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertNotNull(state.savedProfile)
        assertEquals("FEMALE", state.savedProfile?.gender)
    }

    private class FakeOnboardingRepository : OnboardingRepository {

        var saveProfileResult: Result<ProfileResult> = Result.failure(IllegalStateException("save failed"))

        override suspend fun saveProfile(
            token: String,
            fullName: String,
            nickName: String?,
            birthDate: String,
            gender: String,
        ): ProfileResult {
            return saveProfileResult.getOrThrow()
        }

        override suspend fun getCoupleStatus(token: String): CoupleStatus {
            return CoupleStatus(
                profileCompleted = true,
                paired = false,
                partnerUsername = null,
                myCoupleCode = null,
                incomingRequestId = null,
                incomingRequesterUsername = null,
                incomingRequesterDisplayName = null,
                incomingCreatedAt = null,
                outgoingRequestId = null,
                outgoingRecipientUsername = null,
                outgoingStatus = null,
                outgoingUpdatedAt = null,
            )
        }

        override suspend fun sendCoupleRequest(token: String, partnerCode: String): CoupleRequestAction {
            return CoupleRequestAction(null, null, "unused", null, null)
        }

        override suspend fun decideCoupleRequest(token: String, requestId: String, accept: Boolean): CoupleRequestAction {
            return CoupleRequestAction(null, null, "unused", null, null)
        }
    }
}
