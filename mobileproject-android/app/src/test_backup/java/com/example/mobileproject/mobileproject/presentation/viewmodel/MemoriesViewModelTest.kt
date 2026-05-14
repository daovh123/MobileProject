package com.example.mobileproject.presentation.viewmodel

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.data.datasource.remote.ApiService
import com.example.mobileproject.data.model.moment.MomentCommentDto
import com.example.mobileproject.data.model.moment.MomentDto
import com.example.mobileproject.data.model.moment.MomentReactionRequestDto
import com.example.mobileproject.data.model.moment.MomentReactionResponseDto
import com.example.mobileproject.data.model.moment.MomentReactionSummaryDto
import com.example.mobileproject.data.model.moment.MomentResponseDto
import com.example.mobileproject.domain.entity.AuthSession
import com.example.mobileproject.domain.repository.OnboardingRepository
import com.example.mobileproject.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class MemoriesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val apiService = mockk<ApiService>()
    private val onboardingRepository = mockk<OnboardingRepository>(relaxed = true)
    private val authSessionStore = mockk<AuthSessionStore>()
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `load updates moments and username from session`() = runTest {
        val session = AuthSession(
            token = "token-1",
            username = "alice",
            email = "alice@example.com",
            profileCompleted = true,
            coupleConnected = true,
            coupleId = "couple-1",
        )
        every { authSessionStore.load() } returns session

        val moment = MomentDto(
            id = "moment-1",
            coupleId = "couple-1",
            title = "Hello",
            imageUrl = "",
            createdAt = "2026-05-14T00:00:00Z",
        )
        coEvery { apiService.getMoments("couple-1", "Bearer token-1") } returns Response.success(listOf(moment))

        val viewModel = MemoriesViewModel(apiService, onboardingRepository, authSessionStore, context)
        viewModel.load("token-1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("alice", state.currentUsername)
        assertEquals(1, state.moments.size)
        assertEquals("moment-1", state.moments.first().id)
    }

    @Test
    fun `toggleReaction updates reaction summary`() = runTest {
        val session = AuthSession(
            token = "token-2",
            username = "bob",
            email = "bob@example.com",
            profileCompleted = true,
            coupleConnected = true,
            coupleId = "couple-2",
        )
        every { authSessionStore.load() } returns session

        val moment = MomentDto(
            id = "moment-2",
            coupleId = "couple-2",
            title = "Hi",
            imageUrl = "",
            createdAt = "2026-05-14T00:00:00Z",
        )
        coEvery { apiService.getMoments("couple-2", "Bearer token-2") } returns Response.success(listOf(moment))

        val reactionResponse = MomentReactionResponseDto(
            momentId = "moment-2",
            viewerReaction = "HEART",
            reactionsCount = 1,
            reactions = listOf(MomentReactionSummaryDto("HEART", 1)),
        )
        coEvery {
            apiService.reactToMoment("moment-2", any<MomentReactionRequestDto>(), "Bearer token-2")
        } returns Response.success(reactionResponse)

        val viewModel = MemoriesViewModel(apiService, onboardingRepository, authSessionStore, context)
        viewModel.load("token-2")
        advanceUntilIdle()

        viewModel.toggleReaction("moment-2", "HEART")
        advanceUntilIdle()

        val updated = viewModel.uiState.value.moments.first()
        assertEquals("HEART", updated.viewerReaction)
        assertEquals(1, updated.reactionsCount)
        assertTrue(updated.reactions.isNotEmpty())
    }

    @Test
    fun `submitComment appends comment and increments count`() = runTest {
        val session = AuthSession(
            token = "token-3",
            username = "claire",
            email = "claire@example.com",
            profileCompleted = true,
            coupleConnected = true,
            coupleId = "couple-3",
        )
        every { authSessionStore.load() } returns session

        val moment = MomentDto(
            id = "moment-3",
            coupleId = "couple-3",
            title = "Hey",
            imageUrl = "",
            createdAt = "2026-05-14T00:00:00Z",
        )
        coEvery { apiService.getMoments("couple-3", "Bearer token-3") } returns Response.success(listOf(moment))
        coEvery { apiService.getMomentComments("moment-3", "Bearer token-3") } returns Response.success(emptyList())

        val comment = MomentCommentDto(
            id = "comment-1",
            momentId = "moment-3",
            authorUsername = "claire",
            content = "Nice",
            createdAt = "2026-05-14T00:00:00Z",
        )
        coEvery { apiService.createMomentComment("moment-3", any(), "Bearer token-3") } returns Response.success(comment)

        val viewModel = MemoriesViewModel(apiService, onboardingRepository, authSessionStore, context)
        viewModel.load("token-3")
        advanceUntilIdle()

        viewModel.openComments("moment-3")
        advanceUntilIdle()
        viewModel.submitComment("Nice")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.comments.size)
        assertEquals("comment-1", state.comments.first().id)
        val updatedMoment = state.moments.first()
        assertEquals(1, updatedMoment.commentsCount)
        assertNotNull(updatedMoment.id)
    }

    @Test
    fun `saveMoment success returns true`() = runTest {
        val session = AuthSession(
            token = "token-4",
            username = "dana",
            email = "dana@example.com",
            profileCompleted = true,
            coupleConnected = true,
            coupleId = "couple-4",
        )
        every { authSessionStore.load() } returns session

        val response = MomentResponseDto(
            success = true,
            message = "ok",
            moment = MomentDto(
                id = "moment-4",
                coupleId = "couple-4",
                title = "Saved",
                imageUrl = "",
                createdAt = "2026-05-14T00:00:00Z",
            )
        )

        coEvery { apiService.createMoment(any(), "Bearer token-4") } returns Response.success(response)
        coEvery { apiService.getMoments("couple-4", "Bearer token-4") } returns Response.success(listOf(response.moment!!))

        val viewModel = MemoriesViewModel(apiService, onboardingRepository, authSessionStore, context)
        viewModel.load("token-4")
        advanceUntilIdle()

        val result = viewModel.saveMoment("Saved", "data:image/jpeg;base64,AA==")
        advanceUntilIdle()

        assertTrue(result)
        assertFalse(viewModel.uiState.value.isSaving)
    }
}
