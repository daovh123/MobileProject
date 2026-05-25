package com.example.mobileproject.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileproject.BuildConfig
import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.domain.entity.ExplorePlan
import com.example.mobileproject.domain.entity.ExplorePlanItem
import com.example.mobileproject.domain.entity.Place
import com.example.mobileproject.domain.usecase.GetExplorePlanUseCase
import com.example.mobileproject.domain.usecase.GetPlaceFilterOptionsUseCase
import com.example.mobileproject.domain.usecase.GetRandomPlaceUseCase
import com.example.mobileproject.domain.usecase.GetVietnamProvincesUseCase
import com.example.mobileproject.domain.usecase.SearchPlacesUseCase
import com.example.mobileproject.domain.usecase.wallet.GetWalletUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
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

enum class ExploreBudgetSource {
    WALLET,
    MANUAL,
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
    val budgetSource: ExploreBudgetSource = ExploreBudgetSource.WALLET,
    val walletBalance: Long? = null,
    val walletLoading: Boolean = false,
    val manualBudgetInput: String = "",
    val peopleCountInput: String = "2",
    val desiredStopsInput: String = "2",
    val recentHistoryPlaces: List<Place> = emptyList(),
    val recentSharedPlanPlaces: List<Place> = emptyList(),
    val recentGeneratedPlanPlaces: List<Place> = emptyList(),
    val explorePlan: ExplorePlan? = null,
    val planItems: List<ExplorePlanItem> = emptyList(),
    val isPlanLoading: Boolean = false,
    val planErrorMessage: String? = null,
    val errorMessage: String? = null,
)

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val searchPlacesUseCase: SearchPlacesUseCase,
    private val getRandomPlaceUseCase: GetRandomPlaceUseCase,
    private val getVietnamProvincesUseCase: GetVietnamProvincesUseCase,
    private val getPlaceFilterOptionsUseCase: GetPlaceFilterOptionsUseCase,
    private val getExplorePlanUseCase: GetExplorePlanUseCase,
    private val getWalletUseCase: GetWalletUseCase,
    private val authSessionStore: AuthSessionStore,
) : ViewModel() {

    private companion object {
        const val DEFAULT_NEARBY_RADIUS_KM = 5.0
        const val EXPLORE_PAGE_SIZE = 12
        const val DEFAULT_EXPLORE_BUDGET = 30_000L
        const val MIN_EXPLORE_COUNT = 1
        const val MAX_EXPLORE_PEOPLE_COUNT = 20
        const val MAX_EXPLORE_STOPS = 5
    }

    private val _uiState = MutableStateFlow(ExploreUiState(isLoading = true))
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()
    private var loadPlacesJob: Job? = null
    private var latestPlacesRequestId: Long = 0
    private var currentExplorePage: Int = 0

    init {
        debugLog("init")
        loadAreaOptions()
        loadWalletBalance()
        loadTrending()
        refreshPlaces()
    }

    fun onQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(
            query = query,
            recentGeneratedPlanPlaces = emptyList(),
        )
    }

    fun onProvinceChanged(province: String) {
        _uiState.value = _uiState.value.copy(
            selectedProvince = province,
            recentGeneratedPlanPlaces = emptyList(),
        )
    }

    fun onTypeChanged(type: ExplorePlaceType) {
        _uiState.value = _uiState.value.copy(
            selectedType = type,
            recentGeneratedPlanPlaces = emptyList(),
        )
        debugLog("onTypeChanged type=${type.value}")
        refreshPlaces()
    }

    fun onNearMeChanged(checked: Boolean) {
        _uiState.value = _uiState.value.copy(
            nearMeOnly = checked,
            recentGeneratedPlanPlaces = emptyList(),
        )
        debugLog("onNearMeChanged checked=$checked")
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
        debugLog("onCurrentLocationUpdated")
        if (_uiState.value.nearMeOnly) {
            refreshPlaces()
        }
    }

    fun onCurrentLocationUnavailable(errorMessage: String?) {
        debugLog("onCurrentLocationUnavailable message=${errorMessage?.take(60)}")
        _uiState.value = _uiState.value.copy(
            currentLat = null,
            currentLng = null,
            nearMeOnly = false,
            errorMessage = errorMessage,
        )
        refreshPlaces()
    }

    fun onMinRatingChanged(minRating: Int?) {
        _uiState.value = _uiState.value.copy(
            selectedMinRating = minRating,
            recentGeneratedPlanPlaces = emptyList(),
        )
        debugLog("onMinRatingChanged minRating=$minRating")
        refreshPlaces()
    }

    fun onRadiusChanged(radiusKmInput: String) {
        _uiState.value = _uiState.value.copy(
            radiusKmInput = radiusKmInput,
            recentGeneratedPlanPlaces = emptyList(),
        )
    }

    fun onBudgetSourceChanged(source: ExploreBudgetSource) {
        _uiState.value = _uiState.value.copy(
            budgetSource = source,
            recentGeneratedPlanPlaces = emptyList(),
            planErrorMessage = null,
        )
    }

    fun onManualBudgetChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            manualBudgetInput = value.filter { it.isDigit() },
            recentGeneratedPlanPlaces = emptyList(),
            planErrorMessage = null,
        )
    }

    fun onPeopleCountChanged(value: String) {
        val sanitized = value.filter { it.isDigit() }
        val normalized = sanitized
            .take(MAX_EXPLORE_PEOPLE_COUNT.toString().length)
            .toIntOrNull()
            ?.coerceAtMost(MAX_EXPLORE_PEOPLE_COUNT)
            ?.toString()
            ?: sanitized.take(MAX_EXPLORE_PEOPLE_COUNT.toString().length)
        _uiState.value = _uiState.value.copy(
            peopleCountInput = normalized,
            recentGeneratedPlanPlaces = emptyList(),
            planErrorMessage = null,
        )
    }

    fun onDesiredStopsChanged(value: String) {
        val sanitized = value.filter { it.isDigit() }
        val normalized = sanitized
            .take(MAX_EXPLORE_STOPS.toString().length)
            .toIntOrNull()
            ?.coerceAtMost(MAX_EXPLORE_STOPS)
            ?.toString()
            ?: sanitized.take(MAX_EXPLORE_STOPS.toString().length)
        _uiState.value = _uiState.value.copy(
            desiredStopsInput = normalized,
            recentGeneratedPlanPlaces = emptyList(),
            planErrorMessage = null,
        )
    }

    fun onHistoryUpdated(history: List<Place>) {
        _uiState.value = _uiState.value.copy(
            recentHistoryPlaces = history.distinctBy { it.id },
        )
    }

    fun onPlanSharedToChat(place: Place) {
        val updatedSharedPlaces = buildList {
            add(place)
            addAll(_uiState.value.recentSharedPlanPlaces)
        }.distinctBy { it.id }.take(10)
        _uiState.value = _uiState.value.copy(recentSharedPlanPlaces = updatedSharedPlaces)
    }

    fun onPlaceViewed(place: Place) {
        val updatedHistory = buildList {
            add(place)
            addAll(_uiState.value.recentHistoryPlaces)
        }.distinctBy { it.id }
        _uiState.value = _uiState.value.copy(recentHistoryPlaces = updatedHistory)
    }

    fun buildExplorePlan() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val effectiveBudget = resolveEffectiveBudget(currentState)
            val peopleCount = currentState.peopleCountInput.toIntOrNull() ?: 2
            val desiredStops = currentState.desiredStopsInput.toIntOrNull() ?: 2
            val recentPlaces = (
                currentState.recentSharedPlanPlaces +
                    currentState.recentHistoryPlaces +
                    currentState.recentGeneratedPlanPlaces
                )
                .distinctBy { it.id }
            val excludedPlaceIds = recentPlaces.map { it.id }.toSet()
            val requestRandomSeed = System.currentTimeMillis()

            if (effectiveBudget <= 0L) {
                _uiState.value = currentState.copy(
                    planErrorMessage = "Can nhap budget hop le de len lich trinh",
                )
                return@launch
            }

            if (peopleCount !in MIN_EXPLORE_COUNT..MAX_EXPLORE_PEOPLE_COUNT) {
                _uiState.value = currentState.copy(
                    planErrorMessage = "So nguoi chi ho tro tu $MIN_EXPLORE_COUNT den $MAX_EXPLORE_PEOPLE_COUNT.",
                )
                return@launch
            }

            if (desiredStops !in MIN_EXPLORE_COUNT..MAX_EXPLORE_STOPS) {
                _uiState.value = currentState.copy(
                    planErrorMessage = "So quan chi ho tro tu $MIN_EXPLORE_COUNT den $MAX_EXPLORE_STOPS.",
                )
                return@launch
            }

            _uiState.value = currentState.copy(
                isPlanLoading = true,
                planErrorMessage = null,
            )

            val normalizedQuery = currentState.query.trim().takeIf { it.isNotBlank() }
            val normalizedProvince = currentState.selectedProvince.trim().takeIf { it.isNotBlank() }
            val minRating = currentState.selectedMinRating?.toDouble()
            val parsedRadius = currentState.radiusKmInput.trim().toDoubleOrNull()?.takeIf { it > 0 }
            val nearLat = if (currentState.nearMeOnly) currentState.currentLat else null
            val nearLng = if (currentState.nearMeOnly) currentState.currentLng else null
            val radiusKm = if (currentState.nearMeOnly) parsedRadius ?: DEFAULT_NEARBY_RADIUS_KM else null

            runCatching {
                getExplorePlanUseCase(
                    randomSeed = requestRandomSeed,
                    budget = effectiveBudget,
                    peopleCount = peopleCount,
                    desiredStops = desiredStops,
                    query = normalizedQuery,
                    province = normalizedProvince,
                    district = null,
                    type = currentState.selectedType.value,
                    minRating = minRating,
                    nearLat = nearLat,
                    nearLng = nearLng,
                    radiusKm = radiusKm,
                    excludePlaceIds = excludedPlaceIds.toList(),
                    viewedPlaceIds = currentState.recentHistoryPlaces.map { it.id }.distinct(),
                    gonePlaceIds = currentState.recentHistoryPlaces.map { it.id }.distinct(),
                    sentPlaceIds = currentState.recentSharedPlanPlaces.map { it.id }.distinct(),
                    recentKeywords = buildRecentKeywords(recentPlaces),
                )
            }.onSuccess { plan ->
                val filteredItems = plan.items
                    .filterNot { excludedPlaceIds.contains(it.place.id) }
                    .mapIndexed { index, item -> item.copy(stopOrder = index + 1) }
                val removedCount = plan.items.size - filteredItems.size
                val filteredPlan = plan.copy(
                    items = filteredItems,
                    summary = plan.summary.copy(
                        estimatedTotalCost = filteredItems.sumOf(ExplorePlanItem::estimatedCost),
                    ),
                )
                val updatedGeneratedPlaces = buildList {
                    addAll(filteredItems.map(ExplorePlanItem::place))
                    addAll(currentState.recentGeneratedPlanPlaces)
                }.distinctBy { it.id }.take(30)
                val budgetCapacityMessage = when {
                    filteredItems.isEmpty() -> "Budget hien tai chua du de goi y quan phu hop."
                    filteredItems.size < desiredStops -> "Budget hien tai chi du de goi y ${filteredItems.size}/$desiredStops quan."
                    else -> null
                }
                _uiState.value = _uiState.value.copy(
                    isPlanLoading = false,
                    explorePlan = filteredPlan,
                    planItems = filteredItems,
                    recentGeneratedPlanPlaces = updatedGeneratedPlaces,
                    planErrorMessage = when {
                        budgetCapacityMessage != null -> budgetCapacityMessage
                        removedCount <= 0 -> null
                        filteredItems.isEmpty() -> "Da bo qua $removedCount dia diem da xem, da di, hoac da duoc goi y. Thu doi bo loc de tim dia diem moi."
                        else -> "Da bo qua $removedCount dia diem da xem, da di, hoac da duoc goi y truoc do."
                    },
                )
            }.onFailure { throwable ->
                warnLog("buildExplorePlan failure", throwable)
                _uiState.value = _uiState.value.copy(
                    isPlanLoading = false,
                    planErrorMessage = throwable.message ?: "Khong the tao goi y budget",
                )
            }
        }
    }

    fun search() {
        debugLog("search")
        refreshPlaces()
    }

    fun retry() {
        debugLog("retry")
        refreshPlaces()
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (state.isLoading || state.isPaging || !state.hasMore) {
            return
        }

        debugLog("loadNextPage currentPage=$currentExplorePage loaded=${state.loadedPlaces} total=${state.totalPlaces}")
        loadPlacesPage(page = currentExplorePage + 1, append = true)
    }

    fun randomPlace() {
        viewModelScope.launch {
            debugLog("randomPlace")
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
                debugLog("randomPlace success placeId=${randomPlace.id}")
                _uiState.value = _uiState.value.copy(
                    isRandomLoading = false,
                    randomSuggestion = randomPlace,
                    randomSuggestionToken = _uiState.value.randomSuggestionToken + 1,
                )
            }.onFailure { throwable ->
                warnLog("randomPlace failure", throwable)
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

    private fun loadWalletBalance() {
        val session = authSessionStore.load()
        val coupleId = session?.coupleId
        val token = session?.token

        if (coupleId.isNullOrBlank() || token.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                budgetSource = ExploreBudgetSource.MANUAL,
                walletLoading = false,
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(walletLoading = true)
            getWalletUseCase(coupleId, token).collectLatest { result ->
                _uiState.value = if (result.isSuccess) {
                    _uiState.value.copy(
                        walletBalance = result.getOrNull()?.balance,
                        walletLoading = false,
                    )
                } else {
                    _uiState.value.copy(
                        budgetSource = ExploreBudgetSource.MANUAL,
                        walletLoading = false,
                    )
                }
            }
        }
    }

    private fun refreshPlaces() {
        debugLog(
            "refreshPlaces nearMe=${_uiState.value.nearMeOnly} type=${_uiState.value.selectedType.value} provinceBlank=${_uiState.value.selectedProvince.isBlank()}",
        )
        currentExplorePage = 0
        loadPlacesPage(page = 0, append = false)
    }

    private fun loadPlacesPage(page: Int, append: Boolean) {
        if (append && page <= 0) {
            return
        }

        debugLog("loadPlacesPage page=$page append=$append")

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
                debugLog(
                    "loadPlacesPage success page=${searchPage.page} items=${searchPage.items.size} total=${searchPage.total}",
                )
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
                warnLog("loadPlacesPage failure", throwable)
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
            debugLog("loadTrending")
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
                debugLog("loadTrending success items=${searchPage.items.size}")
                _uiState.value = _uiState.value.copy(
                    trending = searchPage.items,
                    trendingLoading = false,
                )
            }.onFailure {
                warnLog("loadTrending failure", it)
                _uiState.value = _uiState.value.copy(trendingLoading = false)
            }
        }
    }

    private fun resolveEffectiveBudget(state: ExploreUiState): Long {
        val manualBudget = state.manualBudgetInput.toLongOrNull()
        return when (state.budgetSource) {
            ExploreBudgetSource.WALLET -> state.walletBalance ?: manualBudget ?: DEFAULT_EXPLORE_BUDGET
            ExploreBudgetSource.MANUAL -> manualBudget ?: DEFAULT_EXPLORE_BUDGET
        }
    }

    private fun buildRecentKeywords(history: List<Place>): List<String> {
        return history.asSequence()
            .take(10)
            .flatMap { place ->
                sequenceOf(
                    place.effectiveTag,
                    place.category,
                    place.mealType,
                    place.name,
                    place.address,
                    place.district,
                    place.province,
                )
            }
            .mapNotNull { it?.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .toList()
    }

    private fun debugLog(message: String) {
        if (!BuildConfig.DEBUG) {
            return
        }
        runCatching { Log.d(EXPLORE_VM_LOG_TAG, message) }
    }

    private fun warnLog(message: String, throwable: Throwable? = null) {
        if (!BuildConfig.DEBUG) {
            return
        }
        runCatching {
            if (throwable == null) {
                Log.w(EXPLORE_VM_LOG_TAG, message)
            } else {
                Log.w(EXPLORE_VM_LOG_TAG, message, throwable)
            }
        }
    }
}
