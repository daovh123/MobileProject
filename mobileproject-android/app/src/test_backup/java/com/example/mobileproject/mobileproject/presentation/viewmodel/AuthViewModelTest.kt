package com.example.mobileproject.presentation.viewmodel

import com.example.mobileproject.domain.entity.AuthSession
import com.example.mobileproject.domain.repository.AuthRepository
import com.example.mobileproject.domain.usecase.LoginUseCase
import com.example.mobileproject.domain.usecase.LogoutUseCase
import com.example.mobileproject.domain.usecase.RegisterUseCase
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
class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `login with blank input sets validation error`() {
        val repository = FakeAuthRepository()
        val viewModel = AuthViewModel(
            loginUseCase = LoginUseCase(repository),
            registerUseCase = RegisterUseCase(repository),
            logoutUseCase = LogoutUseCase(repository),
        )

        viewModel.login("", "")

        val state = viewModel.uiState.value
        assertEquals("Vui long nhap day du thong tin", state.errorMessage)
        assertFalse(state.isLoading)
        assertNull(state.authSession)
    }

    @Test
    fun `login success updates auth session`() = runTest {
        val repository = FakeAuthRepository().apply {
            loginResult = Result.success(
                AuthSession(
                    token = "token-abc",
                    username = "alice",
                    email = "alice@example.com",
                    profileCompleted = false,
                    coupleConnected = false,
                )
            )
        }
        val viewModel = AuthViewModel(
            loginUseCase = LoginUseCase(repository),
            registerUseCase = RegisterUseCase(repository),
            logoutUseCase = LogoutUseCase(repository),
        )

        viewModel.login("alice@example.com", "secret")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertNotNull(state.authSession)
        assertEquals("alice", state.authSession?.username)
    }

    @Test
    fun `register failure exposes repository error message`() = runTest {
        val repository = FakeAuthRepository().apply {
            registerResult = Result.failure(IllegalStateException("Email da ton tai"))
        }
        val viewModel = AuthViewModel(
            loginUseCase = LoginUseCase(repository),
            registerUseCase = RegisterUseCase(repository),
            logoutUseCase = LogoutUseCase(repository),
        )

        viewModel.register("alice", "alice@example.com", "secret")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Email da ton tai", state.errorMessage)
        assertNull(state.authSession)
    }

    @Test
    fun `logout success sets logoutCompleted`() = runTest {
        val repository = FakeAuthRepository().apply {
            logoutResult = Result.success(Unit)
        }
        val viewModel = AuthViewModel(
            loginUseCase = LoginUseCase(repository),
            registerUseCase = RegisterUseCase(repository),
            logoutUseCase = LogoutUseCase(repository),
        )

        viewModel.logout("token-1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertTrue(state.logoutCompleted)
        assertEquals(1, repository.logoutCalls)
    }

    @Test
    fun `logout failure still completes logout flow`() = runTest {
        val repository = FakeAuthRepository().apply {
            logoutResult = Result.failure(IllegalStateException("Missing or invalid access token"))
        }
        val viewModel = AuthViewModel(
            loginUseCase = LoginUseCase(repository),
            registerUseCase = RegisterUseCase(repository),
            logoutUseCase = LogoutUseCase(repository),
        )

        viewModel.logout("stale-token")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertTrue(state.logoutCompleted)
        assertEquals(1, repository.logoutCalls)
    }

    @Test
    fun `logout with blank token still completes logout flow`() = runTest {
        val repository = FakeAuthRepository().apply {
            logoutResult = Result.failure(IllegalStateException("should not be called"))
        }
        val viewModel = AuthViewModel(
            loginUseCase = LoginUseCase(repository),
            registerUseCase = RegisterUseCase(repository),
            logoutUseCase = LogoutUseCase(repository),
        )

        viewModel.logout("   ")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertTrue(state.logoutCompleted)
        assertEquals(0, repository.logoutCalls)
    }

    private class FakeAuthRepository : AuthRepository {

        var loginResult: Result<AuthSession> = Result.failure(IllegalStateException("Login failed"))
        var registerResult: Result<AuthSession> = Result.failure(IllegalStateException("Register failed"))
        var logoutResult: Result<Unit> = Result.success(Unit)
        var logoutCalls: Int = 0

        override suspend fun login(usernameOrEmail: String, password: String): AuthSession {
            return loginResult.getOrThrow()
        }

        override suspend fun register(username: String, email: String, password: String): AuthSession {
            return registerResult.getOrThrow()
        }

        override suspend fun logout(token: String) {
            logoutCalls += 1
            logoutResult.getOrThrow()
        }
    }
}
