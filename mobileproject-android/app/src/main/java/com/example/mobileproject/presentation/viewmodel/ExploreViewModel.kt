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
    val openNowOnly: Boolean = false,
    val selectedMinRating: Int? = null,
    val radiusKmInput: String = "",
    val randomSuggestion: Place? = null,
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
    }

    fun onOpenNowChanged(checked: Boolean) {
        _uiState.value = _uiState.value.copy(openNowOnly = checked)
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

            runCatching {
                getRandomPlaceUseCase(
                    query = normalizedQuery,
                    province = normalizedProvince,
                    district = null,
                    type = state.selectedType.value,
                    openNow = if (state.openNowOnly) true else null,
                    minRating = minRating,
                )
            }.onSuccess { randomPlace ->
                _uiState.value = _uiState.value.copy(
                    isRandomLoading = false,
                    randomSuggestion = randomPlace,
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

            runCatching {
                searchPlacesUseCase(
                    query = normalizedQuery,
                    province = normalizedProvince,
                    district = null,
                    type = state.selectedType.value,
                    openNow = if (state.openNowOnly) true else null,
                    minRating = minRating,
                    page = 0,
                    size = 30,
                    sort = "trending",
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