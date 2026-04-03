package com.example.mobileproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileproject.domain.entity.Place
import com.example.mobileproject.domain.usecase.GetPlaceFilterOptionsUseCase
import com.example.mobileproject.domain.usecase.GetRandomPlaceUseCase
import com.example.mobileproject.domain.usecase.GetVietnamProvincesUseCase
import com.example.mobileproject.domain.usecase.SearchPlacesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

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
    val places: List<Place> = emptyList(),
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
    }

    private val _uiState = MutableStateFlow(ExploreUiState(isLoading = true))
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    init {
        loadAreaOptions()
        loadPlaces()
    }

    fun onQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
    }

    fun onProvinceChanged(province: String) {
        _uiState.value = _uiState.value.copy(selectedProvince = province)
    }

    fun onTypeChanged(type: ExplorePlaceType) {
        _uiState.value = _uiState.value.copy(selectedType = type)
        loadPlaces()
    }

    fun onNearMeChanged(checked: Boolean) {
        _uiState.value = _uiState.value.copy(nearMeOnly = checked)
        if (!checked || (_uiState.value.currentLat != null && _uiState.value.currentLng != null)) {
            loadPlaces()
        }
    }

    fun onCurrentLocationUpdated(lat: Double, lng: Double) {
        _uiState.value = _uiState.value.copy(
            currentLat = lat,
            currentLng = lng,
            errorMessage = null,
        )
        if (_uiState.value.nearMeOnly) {
            loadPlaces()
        }
    }

    fun onCurrentLocationUnavailable(errorMessage: String?) {
        _uiState.value = _uiState.value.copy(
            currentLat = null,
            currentLng = null,
            nearMeOnly = false,
            errorMessage = errorMessage,
        )
        loadPlaces()
    }

    fun onMinRatingChanged(minRating: Int?) {
        _uiState.value = _uiState.value.copy(selectedMinRating = minRating)
        loadPlaces()
    }

    fun onRadiusChanged(radiusKmInput: String) {
        _uiState.value = _uiState.value.copy(radiusKmInput = radiusKmInput)
    }

    fun search() {
        loadPlaces()
    }

    fun retry() {
        loadPlaces()
    }

    fun randomPlace() {
        viewModelScope.launch {
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
                _uiState.value = _uiState.value.copy(
                    isRandomLoading = false,
                    randomSuggestion = randomPlace,
                    randomSuggestionToken = _uiState.value.randomSuggestionToken + 1,
                )
            }.onFailure { throwable ->
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

    private fun loadPlaces() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

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
                state.selectedMinRating != null -> "ratingMix"
                else -> "trending"
            }

            if (state.nearMeOnly && (nearLat == null || nearLng == null)) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    places = emptyList(),
                    errorMessage = "Can cap quyen vi tri de loc gan toi",
                )
                return@launch
            }

            runCatching {
                searchPlacesUseCase(
                    query = normalizedQuery,
                    province = normalizedProvince,
                    district = null,
                    type = state.selectedType.value,
                    minRating = minRating,
                    nearLat = nearLat,
                    nearLng = nearLng,
                    radiusKm = radiusKm,
                    page = 0,
                    size = 30,
                    sort = sort,
                )
            }.onSuccess { places ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    places = places,
                    errorMessage = null,
                )
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    places = emptyList(),
                    errorMessage = throwable.message ?: "Khong the tai du lieu",
                )
            }
        }
    }
}