package com.example.mobileproject.presentation.viewmodel

import com.example.mobileproject.domain.entity.Place
import com.example.mobileproject.domain.entity.PlaceFilterOptions
import com.example.mobileproject.domain.repository.PlaceRepository
import com.example.mobileproject.domain.usecase.GetPlaceFilterOptionsUseCase
import com.example.mobileproject.domain.usecase.GetRandomPlaceUseCase
import com.example.mobileproject.domain.usecase.GetVietnamProvincesUseCase
import com.example.mobileproject.domain.usecase.SearchPlacesUseCase
import com.example.mobileproject.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExploreViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `init loads places successfully`() = runTest {
        val repository = FakePlaceRepository().apply {
            result = Result.success(
                listOf(
                    Place(
                        id = "p1",
                        name = "Pho Ong Cat",
                        address = "201 Pham Ngu Lao",
                        district = "Quan 1",
                        province = "Thanh pho Ho Chi Minh",
                        effectiveTag = "Bun / Pho",
                        category = "Quan an",
                        mealType = "Bua toi",
                        rating = 4.6,
                        reviewCount = 5002,
                        openHours = "07:00 - 22:00",
                        priceRange = "100000-200000",
                        imageUrl = null,
                        googleMapsUrl = null,
                        lat = 10.77,
                        lng = 106.69,
                    )
                )
            )
            provincesResult = Result.success(listOf("Ha Noi", "Thanh pho Ho Chi Minh"))
        }

        val viewModel = ExploreViewModel(
            searchPlacesUseCase = SearchPlacesUseCase(repository),
            getRandomPlaceUseCase = GetRandomPlaceUseCase(repository),
            getVietnamProvincesUseCase = GetVietnamProvincesUseCase(repository),
            getPlaceFilterOptionsUseCase = GetPlaceFilterOptionsUseCase(repository),
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals(1, state.places.size)
        assertEquals("p1", state.places.first().id)
        assertEquals(2, state.availableProvinces.size)
    }

    @Test
    fun `search failure exposes error message`() = runTest {
        val repository = FakePlaceRepository().apply {
            result = Result.success(emptyList())
        }
        val viewModel = ExploreViewModel(
            searchPlacesUseCase = SearchPlacesUseCase(repository),
            getRandomPlaceUseCase = GetRandomPlaceUseCase(repository),
            getVietnamProvincesUseCase = GetVietnamProvincesUseCase(repository),
            getPlaceFilterOptionsUseCase = GetPlaceFilterOptionsUseCase(repository),
        )
        advanceUntilIdle()

        repository.result = Result.failure(IllegalStateException("Timeout"))

        viewModel.onQueryChanged("bun bo")
        viewModel.search()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.places.isEmpty())
        assertEquals("Timeout", state.errorMessage)
        assertEquals("bun bo", repository.lastQuery)
    }

    @Test
    fun `random place success updates random suggestion`() = runTest {
        val randomPlace = Place(
            id = "p-random",
            name = "Quan Random",
            address = "123 Random Street",
            district = "Q. 1",
            province = "Thanh pho Ho Chi Minh",
            effectiveTag = "Quan an",
            category = "Quan an",
            mealType = "Bua toi",
            rating = 4.8,
            reviewCount = 120,
            openHours = "09:00 - 21:00",
            priceRange = "100000-200000",
            imageUrl = null,
            googleMapsUrl = null,
            lat = 10.77,
            lng = 106.69,
        )
        val repository = FakePlaceRepository().apply {
            result = Result.success(emptyList())
            randomResult = Result.success(randomPlace)
        }
        val viewModel = ExploreViewModel(
            searchPlacesUseCase = SearchPlacesUseCase(repository),
            getRandomPlaceUseCase = GetRandomPlaceUseCase(repository),
            getVietnamProvincesUseCase = GetVietnamProvincesUseCase(repository),
            getPlaceFilterOptionsUseCase = GetPlaceFilterOptionsUseCase(repository),
        )
        advanceUntilIdle()

        viewModel.randomPlace()
        advanceUntilIdle()

        assertEquals("p-random", viewModel.uiState.value.randomSuggestion?.id)
    }

    private class FakePlaceRepository : PlaceRepository {

        var result: Result<List<Place>> = Result.success(emptyList())
        var randomResult: Result<Place> = Result.failure(IllegalStateException("No random"))
        var filterOptionsResult: Result<PlaceFilterOptions> = Result.success(PlaceFilterOptions())
        var provincesResult: Result<List<String>> = Result.success(emptyList())
        var lastQuery: String? = null

        override suspend fun searchPlaces(
            query: String?,
            province: String?,
            district: String?,
            type: String,
            minRating: Double?,
            nearLat: Double?,
            nearLng: Double?,
            radiusKm: Double?,
            page: Int,
            size: Int,
            sort: String,
        ): List<Place> {
            lastQuery = query
            return result.getOrThrow()
        }

        override suspend fun getRandomPlace(
            query: String?,
            province: String?,
            district: String?,
            type: String,
            minRating: Double?,
            nearLat: Double?,
            nearLng: Double?,
            radiusKm: Double?,
        ): Place {
            return randomResult.getOrThrow()
        }

        override suspend fun getFilterOptions(): PlaceFilterOptions {
            return filterOptionsResult.getOrThrow()
        }

        override suspend fun getVietnamProvinces(): List<String> {
            return provincesResult.getOrThrow()
        }
    }
}