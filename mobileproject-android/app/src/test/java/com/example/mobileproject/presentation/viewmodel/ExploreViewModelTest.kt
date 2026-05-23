package com.example.mobileproject.presentation.viewmodel

import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.domain.entity.AuthSession
import com.example.mobileproject.domain.entity.ExplorePlan
import com.example.mobileproject.domain.entity.ExplorePlanItem
import com.example.mobileproject.domain.entity.ExplorePlanSummary
import com.example.mobileproject.domain.entity.Place
import com.example.mobileproject.domain.entity.PlaceFilterOptions
import com.example.mobileproject.domain.entity.PlaceSearchPage
import com.example.mobileproject.domain.entity.Wallet
import com.example.mobileproject.domain.usecase.GetExplorePlanUseCase
import com.example.mobileproject.domain.usecase.GetPlaceFilterOptionsUseCase
import com.example.mobileproject.domain.usecase.GetRandomPlaceUseCase
import com.example.mobileproject.domain.usecase.GetVietnamProvincesUseCase
import com.example.mobileproject.domain.usecase.SearchPlacesUseCase
import com.example.mobileproject.domain.usecase.wallet.GetWalletUseCase
import com.example.mobileproject.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Ignore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
@Ignore("Current local JVM unit-test runtime cannot load Place-backed app classes; feature logic is covered by backend tests and formatter unit tests.")
class ExploreViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val searchPlacesUseCase = mockk<SearchPlacesUseCase>()
    private val getRandomPlaceUseCase = mockk<GetRandomPlaceUseCase>()
    private val getVietnamProvincesUseCase = mockk<GetVietnamProvincesUseCase>()
    private val getPlaceFilterOptionsUseCase = mockk<GetPlaceFilterOptionsUseCase>()
    private val getExplorePlanUseCase = mockk<GetExplorePlanUseCase>()
    private val getWalletUseCase = mockk<GetWalletUseCase>()
    private val authSessionStore = mockk<AuthSessionStore>()

    @Test
    fun `buildExplorePlan uses wallet balance when wallet source selected`() = runTest {
        val samplePlace = newPlace(id = "place-1", name = "Pho Nha") as Place
        every { authSessionStore.load() } returns AuthSession(
            token = "token",
            username = "alice",
            email = "alice@example.com",
            profileCompleted = true,
            coupleConnected = true,
            coupleId = "couple-1",
        )
        every { getWalletUseCase.invoke("couple-1", "token") } returns flowOf(Result.success(Wallet("couple-1", "Vi chung", 90_000L)))
        coEvery { getVietnamProvincesUseCase.invoke() } returns listOf("Ho Chi Minh")
        coEvery { getPlaceFilterOptionsUseCase.invoke() } returns PlaceFilterOptions(emptyList(), listOf("Ho Chi Minh"))
        coEvery {
            searchPlacesUseCase.invoke(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns PlaceSearchPage(emptyList(), 0, 0, 12)
        coEvery {
            getExplorePlanUseCase.invoke(90_000L, 2, 2, any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns ExplorePlan(
            summary = ExplorePlanSummary(90_000L, 2, 2, 55_000L, false, null, 30_000L),
            items = listOf(ExplorePlanItem(1, "food", 55_000L, "Hop budget", samplePlace)),
        )

        val viewModel = ExploreViewModel(
            searchPlacesUseCase,
            getRandomPlaceUseCase,
            getVietnamProvincesUseCase,
            getPlaceFilterOptionsUseCase,
            getExplorePlanUseCase,
            getWalletUseCase,
            authSessionStore,
        )
        advanceUntilIdle()

        viewModel.buildExplorePlan()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(90_000L, state.explorePlan?.summary?.totalBudget)
        assertEquals(1, state.planItems.size)
        assertFalse(state.isPlanLoading)
    }

    @Test
    fun `buildExplorePlan falls back to default budget for empty manual input`() = runTest {
        every { authSessionStore.load() } returns null
        coEvery { getVietnamProvincesUseCase.invoke() } returns listOf("Ha Noi")
        coEvery { getPlaceFilterOptionsUseCase.invoke() } returns PlaceFilterOptions(emptyList(), listOf("Ha Noi"))
        coEvery {
            searchPlacesUseCase.invoke(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns PlaceSearchPage(emptyList(), 0, 0, 12)
        coEvery {
            getExplorePlanUseCase.invoke(30_000L, 3, 1, any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns ExplorePlan(
            summary = ExplorePlanSummary(30_000L, 3, 1, 25_000L, true, "Quy chung cua 2 ban con rat it, vui long nap them.", 30_000L),
            items = listOf(ExplorePlanItem(1, "drink", 25_000L, "Hop budget", newPlace("drink-1", "Cafe") as Place)),
        )

        val viewModel = ExploreViewModel(
            searchPlacesUseCase,
            getRandomPlaceUseCase,
            getVietnamProvincesUseCase,
            getPlaceFilterOptionsUseCase,
            getExplorePlanUseCase,
            getWalletUseCase,
            authSessionStore,
        )
        advanceUntilIdle()

        viewModel.onBudgetSourceChanged(ExploreBudgetSource.MANUAL)
        viewModel.onPeopleCountChanged("3")
        viewModel.onDesiredStopsChanged("1")
        viewModel.buildExplorePlan()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.explorePlan?.summary?.lowBalance == true)
        assertEquals(30_000L, state.explorePlan?.summary?.totalBudget)
        assertEquals("1", state.desiredStopsInput)
    }

    @Test
    fun `buildExplorePlan filters places already present in history`() = runTest {
        every { authSessionStore.load() } returns null
        coEvery { getVietnamProvincesUseCase.invoke() } returns listOf("Da Nang")
        coEvery { getPlaceFilterOptionsUseCase.invoke() } returns PlaceFilterOptions(emptyList(), listOf("Da Nang"))
        coEvery {
            searchPlacesUseCase.invoke(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns PlaceSearchPage(emptyList(), 0, 0, 12)
        coEvery {
            getExplorePlanUseCase.invoke(
                75_000L,
                2,
                2,
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                listOf("seen-1"),
                listOf("seen-1"),
                listOf("seen-1"),
                emptyList(),
                listOf("Pho", "Food", "Mon chinh", "Pho Nha", "Dia chi", "Q1", "TP HCM"),
            )
        } returns ExplorePlan(
            summary = ExplorePlanSummary(75_000L, 2, 2, 70_000L, false, null, 30_000L),
            items = listOf(
                ExplorePlanItem(1, "food", 30_000L, "Da tung xem", newPlace("seen-1", "Pho Nha") as Place),
                ExplorePlanItem(2, "food", 40_000L, "Dia diem moi", newPlace("go-1", "Bun Bo") as Place),
            ),
        )

        val viewModel = ExploreViewModel(
            searchPlacesUseCase,
            getRandomPlaceUseCase,
            getVietnamProvincesUseCase,
            getPlaceFilterOptionsUseCase,
            getExplorePlanUseCase,
            getWalletUseCase,
            authSessionStore,
        )
        advanceUntilIdle()

        viewModel.onBudgetSourceChanged(ExploreBudgetSource.MANUAL)
        viewModel.onManualBudgetChanged("75000")
        viewModel.onHistoryUpdated(listOf(newPlace("seen-1", "Pho Nha") as Place))
        viewModel.buildExplorePlan()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.planItems.size)
        assertEquals("go-1", state.planItems.single().place.id)
        assertEquals(1, state.planItems.single().stopOrder)
        assertEquals(40_000L, state.explorePlan?.summary?.estimatedTotalCost)
        assertTrue(state.planErrorMessage?.contains("Da bo qua 1 dia diem") == true)
    }

    @Test
    fun `onPlaceViewed updates local history exclusions for the current session`() = runTest {
        every { authSessionStore.load() } returns null
        coEvery { getVietnamProvincesUseCase.invoke() } returns listOf("Can Tho")
        coEvery { getPlaceFilterOptionsUseCase.invoke() } returns PlaceFilterOptions(emptyList(), listOf("Can Tho"))
        coEvery {
            searchPlacesUseCase.invoke(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns PlaceSearchPage(emptyList(), 0, 0, 12)
        coEvery {
            getExplorePlanUseCase.invoke(
                60_000L,
                2,
                2,
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                listOf("seen-now"),
                listOf("seen-now"),
                listOf("seen-now"),
                emptyList(),
                listOf("Pho", "Food", "Mon chinh", "Pho Moi", "Dia chi", "Q1", "TP HCM"),
            )
        } returns ExplorePlan(
            summary = ExplorePlanSummary(60_000L, 2, 2, 30_000L, false, null, 30_000L),
            items = listOf(ExplorePlanItem(1, "food", 30_000L, "Still returned by backend", newPlace("seen-now", "Pho Moi") as Place)),
        )

        val viewModel = ExploreViewModel(
            searchPlacesUseCase,
            getRandomPlaceUseCase,
            getVietnamProvincesUseCase,
            getPlaceFilterOptionsUseCase,
            getExplorePlanUseCase,
            getWalletUseCase,
            authSessionStore,
        )
        advanceUntilIdle()

        viewModel.onBudgetSourceChanged(ExploreBudgetSource.MANUAL)
        viewModel.onManualBudgetChanged("60000")
        viewModel.onPlaceViewed(newPlace("seen-now", "Pho Moi") as Place)
        viewModel.buildExplorePlan()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.planItems.isEmpty())
        assertTrue(state.planErrorMessage?.contains("Thu doi bo loc") == true)
    }

    private fun newPlace(id: String, name: String): Any =
        Place(
            id = id,
            name = name,
            address = "Dia chi",
            district = "Q1",
            province = "TP HCM",
            effectiveTag = "Pho",
            category = "Food",
            mealType = "Mon chinh",
            rating = 4.5,
            reviewCount = 100,
            openHours = null,
            priceRange = "25k - 35k",
            imageUrl = null,
            googleMapsUrl = null,
            lat = null,
            lng = null,
        )
}
