package com.example.mobileproject.presentation.viewmodel

import com.example.mobileproject.data.datasource.local.UserSettingsStore
import com.example.mobileproject.testutil.MainDispatcherRule
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserSettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun makeStore(
        push: Boolean = true,
        email: Boolean = false,
        activity: Boolean = true,
        searchable: Boolean = true,
    ): UserSettingsStore = mockk(relaxed = true) {
        every { pushNotifications } returns flowOf(push)
        every { emailNotifications } returns flowOf(email)
        every { showActivityStatus } returns flowOf(activity)
        every { searchableByEmail } returns flowOf(searchable)
    }

    @Test
    fun `push notifications default true`() = runTest {
        val vm = UserSettingsViewModel(makeStore(push = true))
        advanceUntilIdle()
        assertTrue(vm.pushNotifications.value)
    }

    @Test
    fun `email notifications default false`() = runTest {
        val vm = UserSettingsViewModel(makeStore(email = false))
        advanceUntilIdle()
        assertFalse(vm.emailNotifications.value)
    }

    @Test
    fun `setPushNotifications calls store`() = runTest {
        val store = makeStore()
        val vm = UserSettingsViewModel(store)
        advanceUntilIdle()

        vm.setPushNotifications(false)
        advanceUntilIdle()

        coVerify { store.setPushNotifications(false) }
    }

    @Test
    fun `setEmailNotifications calls store`() = runTest {
        val store = makeStore()
        val vm = UserSettingsViewModel(store)
        advanceUntilIdle()

        vm.setEmailNotifications(true)
        advanceUntilIdle()

        coVerify { store.setEmailNotifications(true) }
    }

    @Test
    fun `showActivityStatus default true`() = runTest {
        val vm = UserSettingsViewModel(makeStore(activity = true))
        advanceUntilIdle()
        assertTrue(vm.showActivityStatus.value)
    }

    @Test
    fun `searchableByEmail default true`() = runTest {
        val vm = UserSettingsViewModel(makeStore(searchable = true))
        advanceUntilIdle()
        assertTrue(vm.searchableByEmail.value)
    }

    @Test
    fun `setShowActivityStatus calls store`() = runTest {
        val store = makeStore()
        val vm = UserSettingsViewModel(store)
        advanceUntilIdle()

        vm.setShowActivityStatus(false)
        advanceUntilIdle()

        coVerify { store.setShowActivityStatus(false) }
    }

    @Test
    fun `setSearchableByEmail calls store`() = runTest {
        val store = makeStore()
        val vm = UserSettingsViewModel(store)
        advanceUntilIdle()

        vm.setSearchableByEmail(false)
        advanceUntilIdle()

        coVerify { store.setSearchableByEmail(false) }
    }
}
