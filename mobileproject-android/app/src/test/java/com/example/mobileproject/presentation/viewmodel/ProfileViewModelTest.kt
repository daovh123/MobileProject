package com.example.mobileproject.presentation.viewmodel

import com.example.mobileproject.domain.entity.AvatarFrame
import com.example.mobileproject.domain.entity.CoupleRequestAction
import com.example.mobileproject.domain.entity.CoupleStatus
import com.example.mobileproject.domain.entity.PartnerProfileSummary
import com.example.mobileproject.domain.entity.ProfileResult
import com.example.mobileproject.domain.repository.OnboardingRepository
import com.example.mobileproject.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `loadProfile fetches partner profile when couple is paired`() = runTest {
        val repository = FakeOnboardingRepository().apply {
            getCoupleStatusResult = Result.success(
                defaultCoupleStatus.copy(
                    paired = true,
                    partnerUsername = "partner_01",
                    daysTogether = 21,
                )
            )
            getPartnerProfileResult = Result.success(
                PartnerProfileSummary(
                    paired = true,
                    message = "ok",
                    username = "partner_01",
                    fullName = "Partner One",
                    nickName = "P1",
                    avatarUrl = null,
                    startAt = "2026-05-01",
                    daysTogether = 21,
                )
            )
        }
        val viewModel = ProfileViewModel(repository)

        viewModel.loadProfile("token-1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isLoadingCouple)
        assertNotNull(state.coupleStatus)
        assertNotNull(state.partnerProfile)
        assertEquals("Partner One", state.partnerProfile?.fullName)
        assertEquals("partner_01", state.partnerProfile?.username)
    }

    private class FakeOnboardingRepository : OnboardingRepository {

        val defaultProfile = ProfileResult(
            username = "alice",
            fullName = "Alice",
            nickName = "Ali",
            birthDate = "2020-02-01",
            gender = "FEMALE",
            profileCompleted = true,
            coupleConnected = false,
        )

        val defaultCoupleStatus = CoupleStatus(
            profileCompleted = true,
            paired = false,
            partnerUsername = null,
            myCoupleCode = null,
            myCoupleCodeExpiresAt = null,
            incomingRequestId = null,
            incomingRequesterUsername = null,
            incomingRequesterDisplayName = null,
            incomingCreatedAt = null,
            outgoingRequestId = null,
            outgoingRecipientUsername = null,
            outgoingStatus = null,
            outgoingUpdatedAt = null,
            coupleId = null,
            startAt = null,
            daysTogether = null,
            anniversaryTomorrow = null,
        )

        var getProfileResult: Result<ProfileResult> = Result.success(defaultProfile)
        var getCoupleStatusResult: Result<CoupleStatus> = Result.success(defaultCoupleStatus)
        var getPartnerProfileResult: Result<PartnerProfileSummary> = Result.failure(
            IllegalStateException("Khong the tai thong tin doi phuong")
        )

        override suspend fun saveProfile(
            token: String,
            fullName: String,
            nickName: String?,
            birthDate: String,
            gender: String,
            email: String?,
        ): ProfileResult = defaultProfile

        override suspend fun getProfile(token: String): ProfileResult {
            return getProfileResult.getOrThrow()
        }

        override suspend fun getCoupleStatus(token: String): CoupleStatus {
            return getCoupleStatusResult.getOrThrow()
        }

        override suspend fun getPartnerProfileSummary(token: String): PartnerProfileSummary {
            return getPartnerProfileResult.getOrThrow()
        }

        override suspend fun sendCoupleRequest(token: String, partnerCode: String): CoupleRequestAction {
            return CoupleRequestAction(null, null, "unused", null, null)
        }

        override suspend fun decideCoupleRequest(
            token: String,
            requestId: String,
            accept: Boolean,
        ): CoupleRequestAction {
            return CoupleRequestAction(null, null, "unused", null, null)
        }

        override suspend fun uploadAvatar(token: String, imageBytes: ByteArray, contentType: String): String {
            return "data:image/png;base64,"
        }

        override suspend fun getAvatarFrames(token: String): List<AvatarFrame> {
            return emptyList()
        }

        override suspend fun setAvatarFrame(token: String, frameId: String?): ProfileResult {
            return defaultProfile.copy(avatarFrameId = frameId)
        }
    }
}
