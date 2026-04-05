package com.example.mobileproject.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileproject.BuildConfig
import com.example.mobileproject.domain.entity.Place
import com.example.mobileproject.domain.usecase.GetPlaceFilterOptionsUseCase
import com.example.mobileproject.domain.usecase.GetRandomPlaceUseCase
import com.example.mobileproject.domain.usecase.GetVietnamProvincesUseCase
import com.example.mobileproject.domain.usecase.SearchPlacesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val EXPLORE_VM_LOG_TAG: String = "ExploreViewModel"

enum class ExplorePlaceType(val value: String) {
    ALL("all"),
    FOOD("food"),
    DRINK("drink"),
}

data class ExploreUiState(
    val query: String = "",
    val selectedProvince: String = "",
    val availableProvinces: List<String> = emptyList(),
    val selectedType: ExplorePlaceType = ExplorePlaceType.ALL,
    val nearMeOnly: Boolean = false,
    val currentLat: Double? = null,
    val currentLng: Double? = null,
    val selectedMinRating: Int? = null,
    val radiusKmInput: String = "",
    val randomSuggestion: Place? = null,
    val randomSuggestionToken: Long = 0,
    val trending: List<Place> = emptyList(),
    val trendingLoading: Boolean = false,
    val places: List<Place> = emptyList(),
    val totalPlaces: Long = 0,
    val loadedPlaces: Int = 0,
    val hasMore: Boolean = false,
    val isPaging: Boolean = false,
    val isLoading: Boolean = false,
    val isRandomLoading: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val searchPlacesUseCase: SearchPlacesUseCase,
    private val getRandomPlaceUseCase: GetRandomPlaceUseCase,
    private val getVietnamProvincesUseCase: GetVietnamProvincesUseCase,
    private val getPlaceFilterOptionsUseCase: GetPlaceFilterOptionsUseCase,
) : ViewModel() {

    private companion object {
        const val DEFAULT_NEARBY_RADIUS_KM = 5.0
        const val EXPLORE_PAGE_SIZE = 30
    }

    private val _uiState = MutableStateFlow(ExploreUiState(isLoading = true))
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()
    private var loadPlacesJob: Job? = null
    private var latestPlacesRequestId: Long = 0
    private var currentExplorePage: Int = 0

    init {
        if (BuildConfig.DEBUG) {
            Log.d(EXPLORE_VM_LOG_TAG, "init")
        }
        loadAreaOptions()
        loadTrending()
        refreshPlaces()
    }

    fun onQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
    }

    fun onProvinceChanged(province: String) {
        _uiState.value = _uiState.value.copy(selectedProvince = province)
    }

    fun onTypeChanged(type: ExplorePlaceType) {
        _uiState.value = _uiState.value.copy(selectedType = type)
        if (BuildConfig.DEBUG) {
            Log.d(EXPLORE_VM_LOG_TAG, "onTypeChanged type=${type.value}")
        }
        refreshPlaces()
    }

    fun onNearMeChanged(checked: Boolean) {
        _uiState.value = _uiState.value.copy(nearMeOnly = checked)
        if (BuildConfig.DEBUG) {
            Log.d(EXPLORE_VM_LOG_TAG, "onNearMeChanged checked=$checked")
        }
        if (!checked || (_uiState.value.currentLat != null && _uiState.value.currentLng != null)) {
            refreshPlaces()
        }
    }

    fun onCurrentLocationUpdated(lat: Double, lng: Double) {
        _uiState.value = _uiState.value.copy(
            currentLat = lat,
            currentLng = lng,
            errorMessage = null,
        )
        if (BuildConfig.DEBUG) {
            Log.d(EXPLORE_VM_LOG_TAG, "onCurrentLocationUpdated")
        }
        if (_uiState.value.nearMeOnly) {
            refreshPlaces()
        }
    }

    fun onCurrentLocationUnavailable(errorMessage: String?) {
        if (BuildConfig.DEBUG) {
            Log.d(EXPLORE_VM_LOG_TAG, "onCurrentLocationUnavailable message=${errorMessage?.take(60)}")
        }
        _uiState.value = _uiState.value.copy(
            currentLat = null,
            currentLng = null,
            nearMeOnly = false,
            errorMessage = errorMessage,
        )
        refreshPlaces()
    }

    fun onMinRatingChanged(minRating: Int?) {
        _uiState.value = _uiState.value.copy(selectedMinRating = minRating)
        if (BuildConfig.DEBUG) {
            Log.d(EXPLORE_VM_LOG_TAG, "onMinRatingChanged minRating=$minRating")
        }
        refreshPlaces()
    }

    fun onRadiusChanged(radiusKmInput: String) {
        _uiState.value = _uiState.value.copy(radiusKmInput = radiusKmInput)
    }

    fun search() {
        if (BuildConfig.DEBUG) {
            Log.d(EXPLORE_VM_LOG_TAG, "search")
        }
        refreshPlaces()
    }

    fun retry() {
        if (BuildConfig.DEBUG) {
            Log.d(EXPLORE_VM_LOG_TAG, "retry")
        }
        refreshPlaces()
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (state.isLoading || state.isPaging || !state.hasMore) {
            return
        }

        if (BuildConfig.DEBUG) {
            Log.d(EXPLORE_VM_LOG_TAG, "loadNextPage currentPage=$currentExplorePage loaded=${state.loadedPlaces} total=${state.totalPlaces}")
        }
        loadPlacesPage(page = currentExplorePage + 1, append = true)
    }

    fun randomPlace() {
        viewModelScope.launch {
            if (BuildConfig.DEBUG) {
                Log.d(EXPLORE_VM_LOG_TAG, "randomPlace")
            }
            _uiState.value = _uiState.value.copy(
                isRandomLoading = true,
                errorMessage = null,
            )

            val state = _uiState.value
            val normalizedQuery = state.query.trim().takeIf { it.isNotBlank() }
            val normalizedProvince = state.selectedProvince.trim().takeIf { it.isNotBlank() }
            val minRating = state.selectedMinRating?.toDouble()
            val parsedRadius = state.radiusKmInput.trim().toDoubleOrNull()?.takeIf { it > 0 }
            val nearLat = if (state.nearMeOnly) state.currentLat else null
            val nearLng = if (state.nearMeOnly) state.currentLng else null
            val radiusKm = if (state.nearMeOnly) parsedRadius ?: DEFAULT_NEARBY_RADIUS_KM else null

            if (state.nearMeOnly && (nearLat == null || nearLng == null)) {
                _uiState.value = state.copy(
                    isRandomLoading = false,
                    errorMessage = "Chua lay duoc vi tri hien tai",
                )
                return@launch
            }

            runCatching {
                getRandomPlaceUseCase(
                    query = normalizedQuery,
                    province = normalizedProvince,
                    district = null,
                    type = state.selectedType.value,
                    minRating = minRating,
                    nearLat = nearLat,
                    nearLng = nearLng,
                    radiusKm = radiusKm,
                )
            }.onSuccess { randomPlace ->
                if (BuildConfig.DEBUG) {
                    Log.d(EXPLORE_VM_LOG_TAG, "randomPlace success placeId=${randomPlace.id}")
                }
                _uiState.value = _uiState.value.copy(
                    isRandomLoading = false,
                    randomSuggestion = randomPlace,
                    randomSuggestionToken = _uiState.value.randomSuggestionToken + 1,
                )
            }.onFailure { throwable ->
                if (BuildConfig.DEBUG) {
                    Log.w(EXPLORE_VM_LOG_TAG, "randomPlace failure", throwable)
                }
                _uiState.value = _uiState.value.copy(
                    isRandomLoading = false,
                    randomSuggestion = null,
                    errorMessage = throwable.message ?: "Khong tim duoc quan phu hop",
                )
            }
        }
    }

    private fun loadAreaOptions() {
        viewModelScope.launch {
            runCatching {
                getVietnamProvincesUseCase()
            }.onSuccess { provinces ->
                _uiState.value = _uiState.value.copy(
                    availableProvinces = provinces,
                )
            }.onFailure {
                runCatching {
                    getPlaceFilterOptionsUseCase()
                }.onSuccess { options ->
                    val fallbackAreas = if (options.provinces.isNotEmpty()) {
                        options.provinces
                    } else {
                        options.districts
                    }

                    _uiState.value = _uiState.value.copy(
                        availableProvinces = fallbackAreas,
                    )
                }
            }
        }
    }

    private fun refreshPlaces() {
        if (BuildConfig.DEBUG) {
            Log.d(
                EXPLORE_VM_LOG_TAG,
                "refreshPlaces nearMe=${_uiState.value.nearMeOnly} type=${_uiState.value.selectedType.value} provinceBlank=${_uiState.value.selectedProvince.isBlank()}",
            )
        }
        currentExplorePage = 0
        loadPlacesPage(page = 0, append = false)
    }

    private fun loadPlacesPage(page: Int, append: Boolean) {
        if (append && page <= 0) {
            return
        }

        if (BuildConfig.DEBUG) {
            Log.d(EXPLORE_VM_LOG_TAG, "loadPlacesPage page=$page append=$append")
        }

        if (!append) {
            loadPlacesJob?.cancel()
        }

        val requestId = if (append) {
            latestPlacesRequestId
        } else {
            ++latestPlacesRequestId
        }

        val beforeRequest = _uiState.value
        _uiState.value = if (append) {
            beforeRequest.copy(isPaging = true, errorMessage = null)
        } else {
            beforeRequest.copy(
                isLoading = true,
                isPaging = false,
                places = emptyList(),
                totalPlaces = 0,
                loadedPlaces = 0,
                hasMore = false,
                errorMessage = null,
            )
        }

        loadPlacesJob = viewModelScope.launch {
            val state = _uiState.value
            val normalizedQuery = state.query.trim().takeIf { it.isNotBlank() }
            val normalizedProvince = state.selectedProvince.trim().takeIf { it.isNotBlank() }
            val minRating = state.selectedMinRating?.toDouble()
            val parsedRadius = state.radiusKmInput.trim().toDoubleOrNull()?.takeIf { it > 0 }
            val nearLat = if (state.nearMeOnly) state.currentLat else null
            val nearLng = if (state.nearMeOnly) state.currentLng else null
            val radiusKm = if (state.nearMeOnly) parsedRadius ?: DEFAULT_NEARBY_RADIUS_KM else null
            val sort = when {
                state.nearMeOnly -> "distance"
                else -> "ratingMix"
            }

            if (state.nearMeOnly && (nearLat == null || nearLng == null)) {
                _uiState.value = if (append) {
                    state.copy(isPaging = false)
                } else {
                    state.copy(
                        isLoading = false,
                        places = emptyList(),
                        totalPlaces = 0,
                        loadedPlaces = 0,
                        hasMore = false,
                        errorMessage = "Can cap quyen vi tri de loc gan toi",
                    )
                }
                return@launch
            }

            try {
                val searchPage = searchPlacesUseCase(
                    query = normalizedQuery,
                    province = normalizedProvince,
                    district = null,
                    type = state.selectedType.value,
                    minRating = minRating,
                    nearLat = nearLat,
                    nearLng = nearLng,
                    radiusKm = radiusKm,
                    page = page,
                    size = EXPLORE_PAGE_SIZE,
                    sort = sort,
                )

                if (requestId != latestPlacesRequestId) {
                    return@launch
                }

                val current = _uiState.value
                val mergedPlaces = if (append) {
                    (current.places + searchPage.items).distinctBy { it.id }
                } else {
                    searchPage.items
                }
                val loadedPlaces = mergedPlaces.size
                val hasMore = loadedPlaces < searchPage.total

                currentExplorePage = searchPage.page
                if (BuildConfig.DEBUG) {
                    Log.d(
                        EXPLORE_VM_LOG_TAG,
                        "loadPlacesPage success page=${searchPage.page} items=${searchPage.items.size} total=${searchPage.total}",
                    )
                }
                _uiState.value = current.copy(
                    isLoading = false,
                    isPaging = false,
                    places = mergedPlaces,
                    totalPlaces = searchPage.total,
                    loadedPlaces = loadedPlaces,
                    hasMore = hasMore,
                    errorMessage = null,
                )
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (throwable: Throwable) {
                if (BuildConfig.DEBUG) {
                    Log.w(EXPLORE_VM_LOG_TAG, "loadPlacesPage failure", throwable)
                }
                if (requestId != latestPlacesRequestId) {
                    return@launch
                }

                val current = _uiState.value
                _uiState.value = if (append) {
                    current.copy(isPaging = false)
                } else {
                    current.copy(
                        isLoading = false,
                        isPaging = false,
                        places = emptyList(),
                        totalPlaces = 0,
                        loadedPlaces = 0,
                        hasMore = false,
                        errorMessage = throwable.message ?: "Khong the tai du lieu",
                    )
                }
            }
        }
    }

    private fun loadTrending() {
        viewModelScope.launch {
            if (BuildConfig.DEBUG) {
                Log.d(EXPLORE_VM_LOG_TAG, "loadTrending")
            }
            _uiState.value = _uiState.value.copy(trendingLoading = true)

            runCatching {
                searchPlacesUseCase(
                    query = null,
                    province = null,
                    district = null,
                    type = ExplorePlaceType.ALL.value,
                    minRating = null,
                    nearLat = null,
                    nearLng = null,
                    radiusKm = null,
                    page = 0,
                    size = 10,
                    sort = "trending",
                )
            }.onSuccess { searchPage ->
                if (BuildConfig.DEBUG) {
                    Log.d(EXPLORE_VM_LOG_TAG, "loadTrending success items=${searchPage.items.size}")
                }
                _uiState.value = _uiState.value.copy(
                    trending = searchPage.items,
                    trendingLoading = false,
                )
            }.onFailure {
                if (BuildConfig.DEBUG) {
                    Log.w(EXPLORE_VM_LOG_TAG, "loadTrending failure", it)
                }
                _uiState.value = _uiState.value.copy(trendingLoading = false)
            }
        }
    }
}