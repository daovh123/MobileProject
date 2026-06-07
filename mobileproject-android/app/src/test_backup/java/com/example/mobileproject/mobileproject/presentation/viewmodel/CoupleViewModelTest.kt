package com.example.mobileproject.presentation.viewmodel

import com.example.mobileproject.domain.entity.AvatarFrame
import com.example.mobileproject.domain.entity.CoupleRequestAction
import com.example.mobileproject.domain.entity.CoupleStatus
import com.example.mobileproject.domain.entity.ProfileResult
import com.example.mobileproject.domain.repository.OnboardingRepository
import com.example.mobileproject.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CoupleViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `startPolling loads initial status with couple code`() = runTest {
        val repository = FakeOnboardingRepository().apply {
            coupleStatusResult = Result.success(
                CoupleStatus(
                    profileCompleted = true,
                    paired = false,
                    partnerUsername = null,
                    myCoupleCode = "482-910",
                    myCoupleCodeExpiresAt = "2030-01-01T00:00:00Z",
                    incomingRequestId = "req-1",
                    incomingRequesterUsername = "alice",
                    incomingRequesterDisplayName = "Alice",
                    incomingCreatedAt = "2026-04-02T00:00:00Z",
                    outgoingRequestId = null,
                    outgoingRecipientUsername = null,
                    outgoingStatus = null,
                    outgoingUpdatedAt = null,
                )
            )
        }
        val viewModel = CoupleViewModel(repository)

        viewModel.startPolling("token-1")
        runCurrent()

        val state = viewModel.uiState.value
        assertEquals("482-910", state.myCoupleCode)
        assertEquals("2030-01-01T00:00:00Z", state.myCoupleCodeExpiresAt)
        assertEquals("req-1", state.incomingRequestId)
        assertFalse(state.paired)

        viewModel.stopPolling()
    }

    @Test
    fun `sendCoupleRequest without token shows session error`() {
        val viewModel = CoupleViewModel(FakeOnboardingRepository())

        viewModel.sendCoupleRequest("123-456")

        val state = viewModel.uiState.value
        assertEquals("Phien dang nhap het han, vui long dang nhap lai", state.errorMessage)
    }

    @Test
    fun `respondIncomingRequest success updates info message`() = runTest {
        val repository = FakeOnboardingRepository().apply {
            coupleStatusResult = Result.success(
                CoupleStatus(
                    profileCompleted = true,
                    paired = false,
                    partnerUsername = null,
                    myCoupleCode = "111-222",
                    incomingRequestId = "req-2",
                    incomingRequesterUsername = "bob",
                    incomingRequesterDisplayName = "Bobby",
                    incomingCreatedAt = null,
                    outgoingRequestId = null,
                    outgoingRecipientUsername = null,
                    outgoingStatus = null,
                    outgoingUpdatedAt = null,
                )
            )
            decideResult = Result.success(
                CoupleRequestAction(
                    requestId = "req-2",
                    status = "ACCEPTED",
                    message = "Couple request accepted",
                    requesterUsername = "bob",
                    recipientUsername = "me",
                )
            )
        }
        val viewModel = CoupleViewModel(repository)

        viewModel.startPolling("token-2")
        runCurrent()

        viewModel.respondIncomingRequest("req-2", accept = true)
        runCurrent()

        val state = viewModel.uiState.value
        assertEquals("Couple request accepted", state.infoMessage)
        assertNull(state.errorMessage)

        viewModel.stopPolling()
    }

    private class FakeOnboardingRepository : OnboardingRepository {

        var coupleStatusResult: Result<CoupleStatus> = Result.success(
            CoupleStatus(
                profileCompleted = false,
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
        )
        var sendRequestResult: Result<CoupleRequestAction> = Result.success(
            CoupleRequestAction(null, "PENDING", "sent", "me", "you")
        )
        var decideResult: Result<CoupleRequestAction> = Result.success(
            CoupleRequestAction(null, "REJECTED", "rejected", "you", "me")
        )

        override suspend fun saveProfile(
            token: String,
            fullName: String,
            nickName: String?,
            birthDate: String,
            gender: String,
            email: String?,
            phoneNumber: String?,
        ): ProfileResult {
            return ProfileResult(null, null, null, null, null, false, false)
        }

        override suspend fun getProfile(token: String): ProfileResult {
            return ProfileResult(
                username = "me",
                fullName = "Me",
                nickName = null,
                birthDate = "2020-01-01",
                gender = "OTHER",
                profileCompleted = true,
                coupleConnected = false,
            )
        }

        override suspend fun getCoupleStatus(token: String): CoupleStatus {
            return coupleStatusResult.getOrThrow()
        }

        override suspend fun sendCoupleRequest(token: String, partnerCode: String): CoupleRequestAction {
            return sendRequestResult.getOrThrow()
        }

        override suspend fun decideCoupleRequest(token: String, requestId: String, accept: Boolean): CoupleRequestAction {
            return decideResult.getOrThrow()
        }

        override suspend fun uploadAvatar(token: String, imageBytes: ByteArray, contentType: String): String {
            return "data:image/png;base64,"
        }

        override suspend fun getAvatarFrames(token: String): List<AvatarFrame> {
            return emptyList()
        }

        override suspend fun setAvatarFrame(token: String, frameId: String?): ProfileResult {
            return getProfile(token).copy(avatarFrameId = frameId)
        }
    }
}
