package com.example.mobileproject.presentation.viewmodel

import com.example.mobileproject.domain.entity.AvatarFrame
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
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `saveProfile with blank fullName sets validation error`() {
        val viewModel = ProfileViewModel(FakeOnboardingRepository())

        viewModel.updateDraft(
            fullName = "",
            nickName = "",
            birthDate = "2020-02-01",
            gender = "FEMALE",
        )
        viewModel.saveProfile("token-1")

        val state = viewModel.uiState.value
        assertEquals("Ho ten khong duoc de trong", state.errorMessage)
        assertFalse(state.isSaving)
        assertNull(state.savedProfile)
    }

    @Test
    fun `loadProfile success updates profile and couple state`() = runTest {
        val repository = FakeOnboardingRepository().apply {
            getProfileResult = Result.success(defaultProfile)
            getCoupleStatusResult = Result.success(defaultCoupleStatus.copy(paired = true, partnerUsername = "partner"))
        }
        val viewModel = ProfileViewModel(repository)

        viewModel.loadProfile("token-1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isLoadingCouple)
        assertNull(state.errorMessage)
        assertEquals("Alice", state.fullName)
        assertEquals("Ali", state.nickName)
        assertEquals("FEMALE", state.gender)
        assertTrue(state.coupleStatus?.paired == true)
    }

    @Test
    fun `updateDraft marks state dirty when fields change`() = runTest {
        val repository = FakeOnboardingRepository().apply {
            getProfileResult = Result.success(defaultProfile)
        }
        val viewModel = ProfileViewModel(repository)

        viewModel.loadProfile("token-1")
        advanceUntilIdle()
        viewModel.updateDraft(
            fullName = "Alice Updated",
            nickName = "Ali",
            birthDate = "2020-02-01",
            gender = "FEMALE",
        )

        val state = viewModel.uiState.value
        assertTrue(state.isDirty)
    }

    @Test
    fun `saveProfile success clears dirty and emits success message`() = runTest {
        val repository = FakeOnboardingRepository().apply {
            getProfileResult = Result.success(defaultProfile)
            saveProfileResult = Result.success(defaultProfile.copy(fullName = "Alice Updated"))
        }
        val viewModel = ProfileViewModel(repository)

        viewModel.loadProfile("token-1")
        advanceUntilIdle()
        viewModel.updateDraft(
            fullName = "Alice Updated",
            nickName = "Ali",
            birthDate = "2020-02-01",
            gender = "FEMALE",
        )
        viewModel.saveProfile("token-1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSaving)
        assertNotNull(state.savedProfile)
        assertFalse(state.isDirty)
        assertEquals("Luu ho so thanh cong", state.saveSuccessMessage)
        assertEquals("Alice Updated", state.savedProfile?.fullName)
    }

    @Test
    fun `saveProfile unauthorized marks sessionExpired`() = runTest {
        val repository = FakeOnboardingRepository().apply {
            getProfileResult = Result.success(defaultProfile)
            saveProfileResult = Result.failure(IllegalStateException("Unauthorized"))
        }
        val viewModel = ProfileViewModel(repository)

        viewModel.loadProfile("token-1")
        advanceUntilIdle()
        viewModel.updateDraft(
            fullName = "Alice Updated",
            nickName = "Ali",
            birthDate = "2020-02-01",
            gender = "FEMALE",
        )
        viewModel.saveProfile("token-1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.sessionExpired)
        assertNull(state.errorMessage)
    }

    @Test
    fun `clearError clears both error and couple error`() = runTest {
        val repository = FakeOnboardingRepository().apply {
            getProfileResult = Result.failure(IllegalStateException("profile failed"))
            getCoupleStatusResult = Result.failure(IllegalStateException("couple failed"))
        }
        val viewModel = ProfileViewModel(repository)

        viewModel.loadProfile("token-1")
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.errorMessage)
        assertNotNull(viewModel.uiState.value.coupleError)

        viewModel.clearError()

        assertNull(viewModel.uiState.value.errorMessage)
        assertNull(viewModel.uiState.value.coupleError)
    }

    @Test
    fun `uploadAvatar success updates avatar url`() = runTest {
        val expectedDataUrl = "data:image/png;base64,AA=="
        val repository = FakeOnboardingRepository().apply {
            uploadAvatarResult = Result.success(expectedDataUrl)
        }
        val viewModel = ProfileViewModel(repository)

        viewModel.uploadAvatar(
            token = "token-1",
            imageBytes = byteArrayOf(1, 2, 3),
            contentType = "image/png",
        )
        advanceUntilIdle()

        for (index in 0 until 50) {
            if (!viewModel.uiState.value.isUploadingAvatar) {
                break
            }
            Thread.sleep(10)
        }

        val state = viewModel.uiState.value
        assertFalse(state.isUploadingAvatar)
        assertNull(state.avatarUploadError)
        assertEquals(expectedDataUrl, state.avatarUrl)
    }

    @Test
    fun `selectFrame success updates frame and closes selector`() = runTest {
        val repository = FakeOnboardingRepository().apply {
            setAvatarFrameResult = Result.success(defaultProfile.copy(avatarFrameId = "frame_rose"))
        }
        val viewModel = ProfileViewModel(repository)

        viewModel.showFrameSelector()
        viewModel.selectFrame("token-1", "frame_rose")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("frame_rose", state.avatarFrameId)
        assertFalse(state.showFrameSelector)
        assertNull(state.avatarUploadError)
    }

    @Test
    fun `loadAvatarFrames success updates available frames`() = runTest {
        val repository = FakeOnboardingRepository().apply {
            getAvatarFramesResult = Result.success(
                listOf(AvatarFrame(id = "frame_gold", name = "Gold", resourceKey = "gold", color = "#FFC107")),
            )
        }
        val viewModel = ProfileViewModel(repository)

        viewModel.loadAvatarFrames("token-1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoadingFrames)
        assertEquals(1, state.availableFrames.size)
        assertEquals("frame_gold", state.availableFrames.first().id)
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
        var saveProfileResult: Result<ProfileResult> = Result.success(defaultProfile)
        var uploadAvatarResult: Result<String> = Result.success("data:image/png;base64,")
        var getAvatarFramesResult: Result<List<AvatarFrame>> = Result.success(emptyList())
        var setAvatarFrameResult: Result<ProfileResult> = Result.success(defaultProfile)

        override suspend fun saveProfile(
            token: String,
            fullName: String,
            nickName: String?,
            birthDate: String,
            gender: String,
            email: String?,
            phoneNumber: String?,
        ): ProfileResult {
            return saveProfileResult.getOrThrow()
        }

        override suspend fun getProfile(token: String): ProfileResult {
            return getProfileResult.getOrThrow()
        }

        override suspend fun getCoupleStatus(token: String): CoupleStatus {
            return getCoupleStatusResult.getOrThrow()
        }

        override suspend fun sendCoupleRequest(token: String, partnerCode: String): CoupleRequestAction {
            return CoupleRequestAction(null, null, "unused", null, null)
        }

        override suspend fun decideCoupleRequest(token: String, requestId: String, accept: Boolean): CoupleRequestAction {
            return CoupleRequestAction(null, null, "unused", null, null)
        }

        override suspend fun uploadAvatar(token: String, imageBytes: ByteArray, contentType: String): String {
            return uploadAvatarResult.getOrThrow()
        }

        override suspend fun getAvatarFrames(token: String): List<AvatarFrame> {
            return getAvatarFramesResult.getOrThrow()
        }

        override suspend fun setAvatarFrame(token: String, frameId: String?): ProfileResult {
            return setAvatarFrameResult.getOrThrow()
        }
    }
}
