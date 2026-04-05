package com.example.mobileproject.presentation.viewmodel

import com.example.mobileproject.domain.entity.Place
import com.example.mobileproject.domain.entity.PlaceFilterOptions
import com.example.mobileproject.domain.entity.PlaceSearchPage
import com.example.mobileproject.domain.repository.PlaceRepository
import com.example.mobileproject.domain.usecase.GetPlaceFilterOptionsUseCase
import com.example.mobileproject.domain.usecase.GetRandomPlaceUseCase
import com.example.mobileproject.domain.usecase.GetVietnamProvincesUseCase
import com.example.mobileproject.domain.usecase.SearchPlacesUseCase
import com.example.mobileproject.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
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
                PlaceSearchPage(
                    items = listOf(
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
                    ),
                    total = 1,
                    page = 0,
                    size = 30,
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
        repository.clearSearchRequests()

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
            result = Result.success(PlaceSearchPage(items = emptyList(), total = 0, page = 0, size = 30))
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
        assertEquals("bun bo", repository.lastSearchRequest?.query)
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
            result = Result.success(PlaceSearchPage(items = emptyList(), total = 0, page = 0, size = 30))
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

    @Test
    fun `onMinRatingChanged uses ratingMix sort and threshold`() = runTest {
        val repository = FakePlaceRepository().apply {
            result = Result.success(PlaceSearchPage(items = emptyList(), total = 0, page = 0, size = 30))
            provincesResult = Result.success(listOf("Ha Noi"))
        }

        val viewModel = ExploreViewModel(
            searchPlacesUseCase = SearchPlacesUseCase(repository),
            getRandomPlaceUseCase = GetRandomPlaceUseCase(repository),
            getVietnamProvincesUseCase = GetVietnamProvincesUseCase(repository),
            getPlaceFilterOptionsUseCase = GetPlaceFilterOptionsUseCase(repository),
        )
        advanceUntilIdle()
        repository.clearSearchRequests()

        viewModel.onMinRatingChanged(4)
        advanceUntilIdle()

        val request = repository.lastSearchRequest
        assertEquals(4.0, request?.minRating)
        assertEquals("ratingMix", request?.sort)
    }

    @Test
    fun `default non-nearby search uses ratingMix sort`() = runTest {
        val repository = FakePlaceRepository().apply {
            result = Result.success(PlaceSearchPage(items = emptyList(), total = 0, page = 0, size = 30))
        }

        val viewModel = ExploreViewModel(
            searchPlacesUseCase = SearchPlacesUseCase(repository),
            getRandomPlaceUseCase = GetRandomPlaceUseCase(repository),
            getVietnamProvincesUseCase = GetVietnamProvincesUseCase(repository),
            getPlaceFilterOptionsUseCase = GetPlaceFilterOptionsUseCase(repository),
        )
        advanceUntilIdle()

        val request = repository.lastSearchRequest
        assertEquals("ratingMix", request?.sort)
    }

    @Test
    fun `near me with location uses distance sort and default radius`() = runTest {
        val repository = FakePlaceRepository().apply {
            result = Result.success(PlaceSearchPage(items = emptyList(), total = 0, page = 0, size = 30))
        }

        val viewModel = ExploreViewModel(
            searchPlacesUseCase = SearchPlacesUseCase(repository),
            getRandomPlaceUseCase = GetRandomPlaceUseCase(repository),
            getVietnamProvincesUseCase = GetVietnamProvincesUseCase(repository),
            getPlaceFilterOptionsUseCase = GetPlaceFilterOptionsUseCase(repository),
        )
        advanceUntilIdle()
        repository.clearSearchRequests()

        viewModel.onCurrentLocationUpdated(10.778, 106.699)
        viewModel.onNearMeChanged(true)
        advanceUntilIdle()

        val request = repository.lastSearchRequest
        assertEquals("distance", request?.sort)
        assertEquals(10.778, request?.nearLat)
        assertEquals(106.699, request?.nearLng)
        assertEquals(5.0, request?.radiusKm)
    }

    @Test
    fun `near me with custom radius uses radius input`() = runTest {
        val repository = FakePlaceRepository().apply {
            result = Result.success(PlaceSearchPage(items = emptyList(), total = 0, page = 0, size = 30))
        }

        val viewModel = ExploreViewModel(
            searchPlacesUseCase = SearchPlacesUseCase(repository),
            getRandomPlaceUseCase = GetRandomPlaceUseCase(repository),
            getVietnamProvincesUseCase = GetVietnamProvincesUseCase(repository),
            getPlaceFilterOptionsUseCase = GetPlaceFilterOptionsUseCase(repository),
        )
        advanceUntilIdle()
        repository.clearSearchRequests()

        viewModel.onRadiusChanged("12.5")
        viewModel.onCurrentLocationUpdated(10.778, 106.699)
        viewModel.onNearMeChanged(true)
        advanceUntilIdle()

        val request = repository.lastSearchRequest
        assertEquals(12.5, request?.radiusKm)
        assertEquals("distance", request?.sort)
    }

    @Test
    fun `loadNextPage appends data until all results are loaded`() = runTest {
        fun createPlace(id: String): Place {
            return Place(
                id = id,
                name = "Place $id",
                address = "Address $id",
                district = "Ha Noi",
                province = "Ha Noi",
                effectiveTag = "Food",
                category = "Food",
                mealType = "Any",
                rating = 4.0,
                reviewCount = 10,
                openHours = "Any",
                priceRange = "Any",
                imageUrl = null,
                googleMapsUrl = null,
                lat = null,
                lng = null,
            )
        }

        val firstPage = (1..30).map { createPlace("p$it") }
        val secondPage = (31..60).map { createPlace("p$it") }
        val thirdPage = (61..65).map { createPlace("p$it") }

        val repository = FakePlaceRepository().apply {
            searchHandler = { request ->
                when (request.page) {
                    0 -> PlaceSearchPage(items = firstPage, total = 65, page = 0, size = 30)
                    1 -> PlaceSearchPage(items = secondPage, total = 65, page = 1, size = 30)
                    else -> PlaceSearchPage(items = thirdPage, total = 65, page = 2, size = 30)
                }
            }
        }

        val viewModel = ExploreViewModel(
            searchPlacesUseCase = SearchPlacesUseCase(repository),
            getRandomPlaceUseCase = GetRandomPlaceUseCase(repository),
            getVietnamProvincesUseCase = GetVietnamProvincesUseCase(repository),
            getPlaceFilterOptionsUseCase = GetPlaceFilterOptionsUseCase(repository),
        )
        advanceUntilIdle()
        repository.clearSearchRequests()

        viewModel.loadNextPage()
        advanceUntilIdle()
        viewModel.loadNextPage()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(65, state.places.size)
        assertEquals(65L, state.totalPlaces)
        assertEquals(65, state.loadedPlaces)
        assertFalse(state.hasMore)
        assertEquals(2, repository.searchRequests.size)
        assertEquals(listOf(1, 2), repository.searchRequests.map { it.page })
    }

    @Test
    fun `changing filters quickly should keep only latest result`() = runTest {
        val strictResult = listOf(
            Place(
                id = "strict",
                name = "Strict result",
                address = "A",
                district = "Ha Noi",
                province = "Ha Noi",
                effectiveTag = "Food",
                category = "Food",
                mealType = "Any",
                rating = 5.0,
                reviewCount = 10,
                openHours = "Any",
                priceRange = "Any",
                imageUrl = null,
                googleMapsUrl = null,
                lat = null,
                lng = null,
            )
        )
        val broadResult = listOf(
            Place(
                id = "broad-1",
                name = "Broad 1",
                address = "B",
                district = "Ha Noi",
                province = "Ha Noi",
                effectiveTag = "Food",
                category = "Food",
                mealType = "Any",
                rating = 4.0,
                reviewCount = 9,
                openHours = "Any",
                priceRange = "Any",
                imageUrl = null,
                googleMapsUrl = null,
                lat = null,
                lng = null,
            ),
            Place(
                id = "broad-2",
                name = "Broad 2",
                address = "C",
                district = "Ha Noi",
                province = "Ha Noi",
                effectiveTag = "Food",
                category = "Food",
                mealType = "Any",
                rating = null,
                reviewCount = null,
                openHours = "Any",
                priceRange = "Any",
                imageUrl = null,
                googleMapsUrl = null,
                lat = null,
                lng = null,
            ),
        )

        val repository = FakePlaceRepository().apply {
            searchHandler = { request ->
                if (request.minRating == 5.0) {
                    delay(200)
                    PlaceSearchPage(
                        items = strictResult,
                        total = strictResult.size.toLong(),
                        page = request.page,
                        size = request.size,
                    )
                } else {
                    delay(10)
                    PlaceSearchPage(
                        items = broadResult,
                        total = broadResult.size.toLong(),
                        page = request.page,
                        size = request.size,
                    )
                }
            }
        }

        val viewModel = ExploreViewModel(
            searchPlacesUseCase = SearchPlacesUseCase(repository),
            getRandomPlaceUseCase = GetRandomPlaceUseCase(repository),
            getVietnamProvincesUseCase = GetVietnamProvincesUseCase(repository),
            getPlaceFilterOptionsUseCase = GetPlaceFilterOptionsUseCase(repository),
        )
        advanceUntilIdle()
        repository.clearSearchRequests()

        viewModel.onMinRatingChanged(5)
        viewModel.onMinRatingChanged(null)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf("broad-1", "broad-2"), state.places.map { it.id })
        assertEquals(null, repository.lastSearchRequest?.minRating)
        assertNull(state.errorMessage)
    }

    @Test
    fun `search should send combined query province type and rating filters`() = runTest {
        val repository = FakePlaceRepository().apply {
            result = Result.success(PlaceSearchPage(items = emptyList(), total = 0, page = 0, size = 30))
        }

        val viewModel = ExploreViewModel(
            searchPlacesUseCase = SearchPlacesUseCase(repository),
            getRandomPlaceUseCase = GetRandomPlaceUseCase(repository),
            getVietnamProvincesUseCase = GetVietnamProvincesUseCase(repository),
            getPlaceFilterOptionsUseCase = GetPlaceFilterOptionsUseCase(repository),
        )
        advanceUntilIdle()
        repository.clearSearchRequests()

        viewModel.onQueryChanged("pho")
        viewModel.onProvinceChanged("ha noi")
        viewModel.onTypeChanged(ExplorePlaceType.FOOD)
        viewModel.onMinRatingChanged(4)
        advanceUntilIdle()

        val request = repository.lastSearchRequest
        assertEquals("pho", request?.query)
        assertEquals("ha noi", request?.province)
        assertEquals("food", request?.type)
        assertEquals(4.0, request?.minRating)
        assertEquals("ratingMix", request?.sort)
    }

    private data class SearchRequestSnapshot(
        val query: String?,
        val province: String?,
        val district: String?,
        val type: String,
        val minRating: Double?,
        val nearLat: Double?,
        val nearLng: Double?,
        val radiusKm: Double?,
        val page: Int,
        val size: Int,
        val sort: String,
    )

    private class FakePlaceRepository : PlaceRepository {

        var result: Result<PlaceSearchPage> = Result.success(PlaceSearchPage())
        var randomResult: Result<Place> = Result.failure(IllegalStateException("No random"))
        var filterOptionsResult: Result<PlaceFilterOptions> = Result.success(PlaceFilterOptions())
        var provincesResult: Result<List<String>> = Result.success(emptyList())
        var searchHandler: (suspend (SearchRequestSnapshot) -> PlaceSearchPage)? = null
        val searchRequests = mutableListOf<SearchRequestSnapshot>()
        val lastSearchRequest: SearchRequestSnapshot?
            get() = searchRequests.lastOrNull()

        fun clearSearchRequests() {
            searchRequests.clear()
        }

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
        ): PlaceSearchPage {
            val request = SearchRequestSnapshot(
                query = query,
                province = province,
                district = district,
                type = type,
                minRating = minRating,
                nearLat = nearLat,
                nearLng = nearLng,
                radiusKm = radiusKm,
                page = page,
                size = size,
                sort = sort,
            )
            searchRequests += request

            val handler = searchHandler
            if (handler != null) {
                return handler(request)
            }

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