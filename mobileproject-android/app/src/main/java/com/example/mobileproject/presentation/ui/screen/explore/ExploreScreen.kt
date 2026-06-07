package com.example.mobileproject.presentation.ui.screen.explore

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.example.mobileproject.BuildConfig
import com.example.mobileproject.R
import com.example.mobileproject.domain.entity.Place
import com.example.mobileproject.presentation.notification.ChatQuickReplySender
import com.example.mobileproject.presentation.ui.components.core.AppEmptyState
import com.example.mobileproject.presentation.ui.components.core.AppFormTextField
import com.example.mobileproject.presentation.ui.components.core.AppPrimaryButton
import com.example.mobileproject.presentation.ui.components.core.AppScreenBackground
import com.example.mobileproject.presentation.ui.components.core.AppSectionHeader
import com.example.mobileproject.presentation.ui.components.core.AppStateMessage
import com.example.mobileproject.presentation.ui.components.core.AppSurfaceCard
import com.example.mobileproject.presentation.ui.components.place.PlaceCard
import com.example.mobileproject.presentation.ui.components.place.TrendingPlaceCard
import com.example.mobileproject.presentation.viewmodel.ExploreBudgetSource
import com.example.mobileproject.presentation.viewmodel.ExplorePlanChatMessageFormatter
import com.example.mobileproject.presentation.viewmodel.ExplorePlaceType
import com.example.mobileproject.presentation.viewmodel.ExploreViewModel
import com.example.mobileproject.presentation.viewmodel.FavoriteViewModel
import com.example.mobileproject.presentation.viewmodel.HistoryViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.util.Locale
import com.example.mobileproject.utils.formatSimpleAmount

private const val LOAD_MORE_THRESHOLD: Int = 6
private const val EXPLORE_LOG_TAG: String = "ExploreScreen"

private data class AreaOption(
    @StringRes val labelResId: Int,
    val query: String,
)

private val predefinedAreaOptions: List<AreaOption> = listOf(
    AreaOption(R.string.explore_area_ha_noi, "ha noi"),
    AreaOption(R.string.explore_area_ho_chi_minh, "ho chi minh"),
    AreaOption(R.string.explore_area_da_nang, "da nang"),
    AreaOption(R.string.explore_area_can_tho, "can tho"),
    AreaOption(R.string.explore_area_hai_phong, "hai phong"),
    AreaOption(R.string.explore_area_nha_trang, "nha trang"),
    AreaOption(R.string.explore_area_hue, "hue"),
    AreaOption(R.string.explore_area_vung_tau, "vung tau"),
    AreaOption(R.string.explore_area_da_lat, "da lat"),
    AreaOption(R.string.explore_area_quy_nhon, "quy nhon"),
    AreaOption(R.string.explore_area_bien_hoa, "bien hoa"),
    AreaOption(R.string.explore_area_buon_ma_thuot, "buon ma thuot"),
    AreaOption(R.string.explore_area_phan_thiet, "phan thiet"),
    AreaOption(R.string.explore_area_long_xuyen, "long xuyen"),
    AreaOption(R.string.explore_area_thai_nguyen, "thai nguyen"),
    AreaOption(R.string.explore_area_nam_dinh, "nam dinh"),
)

@Composable
fun ExploreRoute(accessToken: String) {
    remember(accessToken) {
        if (BuildConfig.DEBUG) {
            Log.d(EXPLORE_LOG_TAG, "route compose start accessTokenBlank=${accessToken.isBlank()}")
        }
        true
    }

    val exploreViewModel: ExploreViewModel = hiltViewModel()
    val favoriteViewModel: FavoriteViewModel = hiltViewModel()
    val historyViewModel: HistoryViewModel = hiltViewModel()

    ExploreScreen(
        accessToken = accessToken,
        exploreViewModel = exploreViewModel,
        favoriteViewModel = favoriteViewModel,
        historyViewModel = historyViewModel,
    )
}

@Composable
fun ExploreScreen(
    accessToken: String,
    exploreViewModel: ExploreViewModel,
    favoriteViewModel: FavoriteViewModel,
    historyViewModel: HistoryViewModel,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by exploreViewModel.uiState.collectAsState()
    val favoriteState by favoriteViewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(Unit) {
        if (BuildConfig.DEBUG) {
            Log.d(EXPLORE_LOG_TAG, "enter accessTokenBlank=${accessToken.isBlank()}")
        }

        onDispose {
            if (BuildConfig.DEBUG) {
                Log.d(EXPLORE_LOG_TAG, "dispose")
            }
        }
    }

    var isAdvancedExpanded by rememberSaveable { mutableStateOf(false) }
    var isTrendingCollapsed by rememberSaveable { mutableStateOf(false) }
    var isBudgetPlannerExpanded by rememberSaveable { mutableStateOf(false) }

    var currentLocationLat by rememberSaveable { mutableStateOf<Double?>(null) }
    var currentLocationLng by rememberSaveable { mutableStateOf<Double?>(null) }

    val fusedLocationClient = remember(context) {
        runCatching { LocationServices.getFusedLocationProviderClient(context) }
            .onFailure {
                if (BuildConfig.DEBUG) {
                    Log.w(EXPLORE_LOG_TAG, "fusedLocationClient init failed", it)
                }
            }
            .getOrNull()
    }

    fun onLocationUnavailable(message: String) {
        currentLocationLat = null
        currentLocationLng = null
        exploreViewModel.onCurrentLocationUnavailable(message)
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun fetchCurrentLocation() {
        val client = fusedLocationClient
        if (client == null) {
            onLocationUnavailable(context.getString(R.string.explore_location_unavailable))
            return
        }

        val cancellationTokenSource = CancellationTokenSource()
        try {
            client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.token)
                .addOnSuccessListener { location ->
                    if (location == null) {
                        onLocationUnavailable(context.getString(R.string.explore_location_unavailable))
                        return@addOnSuccessListener
                    }

                    if (BuildConfig.DEBUG) {
                        Log.d(EXPLORE_LOG_TAG, "location acquired lat=${location.latitude} lng=${location.longitude}")
                    }

                    currentLocationLat = location.latitude
                    currentLocationLng = location.longitude
                    exploreViewModel.onCurrentLocationUpdated(location.latitude, location.longitude)
                }
                .addOnFailureListener { throwable ->
                    if (BuildConfig.DEBUG) {
                        Log.w(EXPLORE_LOG_TAG, "location failure", throwable)
                    }
                    onLocationUnavailable(context.getString(R.string.explore_location_unavailable))
                }
        } catch (security: SecurityException) {
            if (BuildConfig.DEBUG) {
                Log.w(EXPLORE_LOG_TAG, "location SecurityException", security)
            }
            onLocationUnavailable(context.getString(R.string.explore_location_permission_denied))
        } catch (throwable: Throwable) {
            if (BuildConfig.DEBUG) {
                Log.w(EXPLORE_LOG_TAG, "location unexpected error", throwable)
            }
            onLocationUnavailable(context.getString(R.string.explore_location_unavailable))
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            fetchCurrentLocation()
        } else {
            onLocationUnavailable(context.getString(R.string.explore_location_permission_denied))
        }
    }

    fun ensureCurrentLocation() {
        val permissionState = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )

        if (BuildConfig.DEBUG) {
            Log.d(EXPLORE_LOG_TAG, "ensureCurrentLocation permissionGranted=${permissionState == PackageManager.PERMISSION_GRANTED}")
        }

        if (permissionState == PackageManager.PERMISSION_GRANTED) {
            fetchCurrentLocation()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    LaunchedEffect(accessToken) {
        if (accessToken.isNotBlank()) {
            favoriteViewModel.ensureLoaded(accessToken)
            historyViewModel.ensureLoaded(accessToken)
        }
    }

    DisposableEffect(lifecycleOwner, accessToken) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && accessToken.isNotBlank()) {
                favoriteViewModel.refreshIfStale(accessToken)
                historyViewModel.refreshIfStale(accessToken)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(historyViewModel) {
        historyViewModel.uiState.collectLatest { historyState ->
            exploreViewModel.onHistoryUpdated(historyState.history)
        }
    }

    val listState = rememberLazyListState()
    LaunchedEffect(listState) {
        observePagingLoadMore(listState) {
            if (BuildConfig.DEBUG) {
                Log.d(EXPLORE_LOG_TAG, "paging loadNextPage")
            }
            exploreViewModel.loadNextPage()
        }
    }

    var selectedPlace by remember { mutableStateOf<Place?>(null) }
    var showPlaceDetail by rememberSaveable { mutableStateOf(false) }
    var lastHandledRandomSuggestionToken by rememberSaveable { mutableStateOf(-1L) }
    var expandedBudgetItemKey by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.randomSuggestionToken, uiState.randomSuggestion) {
        val suggestion = uiState.randomSuggestion
        val token = uiState.randomSuggestionToken
        if (suggestion != null && token != lastHandledRandomSuggestionToken) {
            if (BuildConfig.DEBUG) {
                Log.d(EXPLORE_LOG_TAG, "randomSuggestion token=$token placeId=${suggestion.id}")
            }
            lastHandledRandomSuggestionToken = token
            selectedPlace = suggestion
            showPlaceDetail = true
        }
    }

    if (showPlaceDetail && selectedPlace != null) {
        PlaceDetailBottomSheet(
            place = selectedPlace!!,
            accessToken = accessToken,
            currentLocationLat = currentLocationLat,
            currentLocationLng = currentLocationLng,
            isFavorite = favoriteState.favoriteIds.contains(selectedPlace!!.id),
            favoriteToggleInProgress = favoriteState.toggleInProgress.contains(selectedPlace!!.id),
            onToggleFavorite = {
                if (accessToken.isBlank()) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.home_pair_session_expired),
                        Toast.LENGTH_SHORT,
                    ).show()
                    return@PlaceDetailBottomSheet
                }
                favoriteViewModel.toggleFavorite(accessToken, selectedPlace!!.id)
            },
            onRecordView = {
                if (accessToken.isNotBlank()) {
                    historyViewModel.recordView(accessToken, selectedPlace!!.id)
                    exploreViewModel.onPlaceViewed(selectedPlace!!)
                    favoriteViewModel.checkFavorite(accessToken, selectedPlace!!.id)
                }
            },
            onDismiss = {
                if (BuildConfig.DEBUG) {
                    Log.d(EXPLORE_LOG_TAG, "dismiss place detail")
                }
                showPlaceDetail = false
            },
        )
    }

    val areaDisplayOptions = remember(context) { buildAreaDisplayOptions(context) }
    val ratingOptions = remember(context) { buildRatingOptions(context) }
    var revealFeed by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(90)
        revealFeed = true
    }

    AppScreenBackground(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            state = listState,
            contentPadding = PaddingValues(bottom = 26.dp, top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                AppSectionHeader(
                    title = stringResource(R.string.explore_feed_title),
                    subtitle = stringResource(R.string.explore_search_hint),
                )
            }

            item {
                AppSurfaceCard {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        ExploreSearchBar(
                            query = uiState.query,
                            isAdvancedExpanded = isAdvancedExpanded,
                            onQueryChange = exploreViewModel::onQueryChanged,
                            onToggleAdvanced = { isAdvancedExpanded = !isAdvancedExpanded },
                            onSearch = { exploreViewModel.search() },
                        )

                        ExploreTypeChipsRow(
                            selectedType = uiState.selectedType,
                            nearMeSelected = uiState.nearMeOnly,
                            onTypeSelected = exploreViewModel::onTypeChanged,
                            onNearMeChanged = { checked ->
                                exploreViewModel.onNearMeChanged(checked)
                                if (checked) {
                                    isAdvancedExpanded = true
                                    ensureCurrentLocation()
                                }
                            },
                        )

                        AnimatedVisibility(visible = isAdvancedExpanded) {
                            ExploreAdvancedFilters(
                                selectedProvinceQuery = uiState.selectedProvince,
                                areaOptions = areaDisplayOptions,
                                onProvinceSelected = { label ->
                                    exploreViewModel.onProvinceChanged(normalizeProvinceSelection(label, context))
                                },
                                selectedMinRating = uiState.selectedMinRating,
                                ratingOptions = ratingOptions,
                                onMinRatingSelected = { index ->
                                    exploreViewModel.onMinRatingChanged(minRatingFromPosition(index))
                                },
                                radiusKmInput = uiState.radiusKmInput,
                                onRadiusChange = exploreViewModel::onRadiusChanged,
                                onRandom = {
                                    if (uiState.nearMeOnly && (uiState.currentLat == null || uiState.currentLng == null)) {
                                        ensureCurrentLocation()
                                    }
                                    exploreViewModel.randomPlace()
                                },
                            )
                        }
                    }
                }
            }

            item {
                ExploreBudgetPlannerCard(
                    isExpanded = isBudgetPlannerExpanded,
                    budgetSource = uiState.budgetSource,
                    walletBalance = uiState.walletBalance,
                    walletLoading = uiState.walletLoading,
                    manualBudgetInput = uiState.manualBudgetInput,
                    peopleCountInput = uiState.peopleCountInput,
                    desiredStopsInput = uiState.desiredStopsInput,
                    isPlanLoading = uiState.isPlanLoading,
                    planErrorMessage = uiState.planErrorMessage,
                    lowBalanceMessage = uiState.explorePlan?.summary?.balanceMessage,
                    onToggleExpanded = {
                        isBudgetPlannerExpanded = !isBudgetPlannerExpanded
                    },
                    onBudgetSourceChanged = exploreViewModel::onBudgetSourceChanged,
                    onManualBudgetChanged = exploreViewModel::onManualBudgetChanged,
                    onPeopleCountChanged = exploreViewModel::onPeopleCountChanged,
                    onDesiredStopsChanged = exploreViewModel::onDesiredStopsChanged,
                    onBuildPlan = exploreViewModel::buildExplorePlan,
                )
            }

            if (uiState.planItems.isNotEmpty()) {
                item {
                    AppSectionHeader(
                        title = stringResource(R.string.explore_budget_results_title),
                        subtitle = uiState.explorePlan?.summary?.let { summary ->
                            stringResource(
                                R.string.explore_budget_results_subtitle,
                                formatSimpleAmount(summary.estimatedTotalCost),
                                formatSimpleAmount(summary.totalBudget),
                            )
                        },
                    )
                }
                items(uiState.planItems, key = { "budget_${it.stopOrder}_${it.place.id}" }) { item ->
                    val itemKey = "budget_${item.stopOrder}_${item.place.id}"
                    ExploreBudgetPlanItemCard(
                        stopOrder = item.stopOrder,
                        title = valueOrUpdating(item.place.name, context),
                        subtitle = item.reason,
                        estimatedCost = item.estimatedCost,
                        experienceType = item.experienceType,
                        isExpanded = expandedBudgetItemKey == itemKey,
                        onToggleExpanded = {
                            expandedBudgetItemKey = if (expandedBudgetItemKey == itemKey) null else itemKey
                        },
                        onViewDetail = {
                            selectedPlace = item.place
                            showPlaceDetail = true
                        },
                        onSendToChat = {
                            if (accessToken.isBlank()) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.home_pair_session_expired),
                                    Toast.LENGTH_SHORT,
                                ).show()
                            } else {
                                coroutineScope.launch {
                                    val sent = ChatQuickReplySender.send(
                                        accessToken = accessToken,
                                        text = ExplorePlanChatMessageFormatter.build(item),
                                    )
                                    if (sent) {
                                        exploreViewModel.onPlanSharedToChat(item.place)
                                    }
                                    Toast.makeText(
                                        context,
                                        context.getString(
                                            if (sent) {
                                                R.string.explore_budget_send_to_chat_success
                                            } else {
                                                R.string.explore_budget_send_to_chat_failed
                                            },
                                        ),
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                }
                            }
                        },
                        onOpenMaps = {
                            val mapsUrl = resolveGoogleMapsUrl(item.place, currentLocationLat, currentLocationLng)
                            if (mapsUrl == null) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.explore_detail_no_map_data),
                                    Toast.LENGTH_SHORT,
                                ).show()
                            } else {
                                openGoogleMaps(context, mapsUrl)
                            }
                        },
                    )
                }
            }

            val randomSuggestion = uiState.randomSuggestion
            if (randomSuggestion != null) {
                item {
                    val areaLabel = randomSuggestion.province
                        ?.takeIf { it.isNotBlank() }
                        ?: randomSuggestion.district
                            ?.takeIf { it.isNotBlank() }
                        ?: stringResource(R.string.explore_updating)

                    AppSurfaceCard {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_restaurant_24),
                                    contentDescription = null,
                                    modifier = Modifier.padding(8.dp),
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stringResource(
                                    R.string.explore_random_result_format,
                                    valueOrUpdating(randomSuggestion.name, context),
                                    areaLabel,
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }

            val hasTrending = uiState.trending.isNotEmpty()
            if (hasTrending) {
                item {
                    AppSurfaceCard {
                        Column(modifier = Modifier.padding(vertical = 10.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = stringResource(R.string.explore_trending_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f),
                                )
                                IconButton(onClick = { isTrendingCollapsed = !isTrendingCollapsed }) {
                                    Icon(
                                        painter = painterResource(
                                            if (isTrendingCollapsed) R.drawable.ic_expand_more_24 else R.drawable.ic_expand_less_24,
                                        ),
                                        contentDescription = stringResource(
                                            if (isTrendingCollapsed) R.string.explore_trending_expand else R.string.explore_trending_collapse,
                                        ),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }

                            AnimatedVisibility(visible = !isTrendingCollapsed) {
                                LazyRow(
                                    contentPadding = PaddingValues(start = 14.dp, end = 14.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    items(uiState.trending, key = { it.id }) { place ->
                                        TrendingPlaceCard(
                                            place = place,
                                            onClick = {
                                                selectedPlace = it
                                                showPlaceDetail = true
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (uiState.loadedPlaces > 0) {
                item {
                    Text(
                        text = stringResource(
                            R.string.explore_results_count,
                            uiState.loadedPlaces,
                            uiState.totalPlaces,
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (uiState.isLoading || uiState.isRandomLoading) {
                item {
                    AppSurfaceCard {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                        )
                    }
                }
            }

            val errorMessage = uiState.errorMessage
            if (!uiState.isLoading && !uiState.isRandomLoading && !errorMessage.isNullOrBlank()) {
                item {
                    AppStateMessage(
                        title = stringResource(R.string.explore_retry),
                        message = stringResource(R.string.explore_error_message, errorMessage),
                        actionText = stringResource(R.string.explore_retry),
                        onAction = exploreViewModel::retry,
                    )
                }
            }

            if (!uiState.isLoading && !uiState.isRandomLoading && errorMessage.isNullOrBlank() && uiState.places.isEmpty()) {
                item {
                    AppEmptyState(
                        title = stringResource(R.string.explore_empty_message),
                        subtitle = stringResource(R.string.explore_random_action),
                    )
                }
            }

            if (revealFeed) {
                items(uiState.places, key = { it.id }) { place ->
                    PlaceCard(
                        place = place,
                        onClick = {
                            selectedPlace = it
                            showPlaceDetail = true
                        },
                    )
                }
            }

            if (uiState.isPaging) {
                item {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
internal fun ExploreBudgetPlannerCard(
    isExpanded: Boolean,
    budgetSource: ExploreBudgetSource,
    walletBalance: Long?,
    walletLoading: Boolean,
    manualBudgetInput: String,
    peopleCountInput: String,
    desiredStopsInput: String,
    isPlanLoading: Boolean,
    planErrorMessage: String?,
    lowBalanceMessage: String?,
    onToggleExpanded: () -> Unit,
    onBudgetSourceChanged: (ExploreBudgetSource) -> Unit,
    onManualBudgetChanged: (String) -> Unit,
    onPeopleCountChanged: (String) -> Unit,
    onDesiredStopsChanged: (String) -> Unit,
    onBuildPlan: () -> Unit,
) {
    AppSurfaceCard {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable(onClick = onToggleExpanded)
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(R.string.explore_budget_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.explore_budget_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Icon(
                        painter = painterResource(
                            if (isExpanded) R.drawable.ic_expand_less_24 else R.drawable.ic_expand_more_24,
                        ),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(10.dp),
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = budgetSource == ExploreBudgetSource.WALLET,
                                onClick = { onBudgetSourceChanged(ExploreBudgetSource.WALLET) },
                                enabled = walletBalance != null || walletLoading,
                                label = { Text(stringResource(R.string.explore_budget_wallet)) },
                                colors = FilterChipDefaults.filterChipColors(),
                            )
                        }
                        item {
                            FilterChip(
                                selected = budgetSource == ExploreBudgetSource.MANUAL,
                                onClick = { onBudgetSourceChanged(ExploreBudgetSource.MANUAL) },
                                label = { Text(stringResource(R.string.explore_budget_manual)) },
                                colors = FilterChipDefaults.filterChipColors(),
                            )
                        }
                    }

                    if (walletLoading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    } else if (walletBalance != null) {
                        Text(
                            text = stringResource(R.string.explore_budget_wallet_balance, formatSimpleAmount(walletBalance)),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    AppFormTextField(
                        value = manualBudgetInput,
                        onValueChange = onManualBudgetChanged,
                        label = stringResource(R.string.explore_budget_input_label),
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(text = stringResource(R.string.explore_budget_input_placeholder))
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next,
                        ),
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AppFormTextField(
                            value = peopleCountInput,
                            onValueChange = onPeopleCountChanged,
                            label = stringResource(R.string.explore_budget_people_label),
                            modifier = Modifier.weight(1f),
                            supporting = {
                                Text(text = stringResource(R.string.explore_budget_people_support))
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next,
                            ),
                        )
                        AppFormTextField(
                            value = desiredStopsInput,
                            onValueChange = onDesiredStopsChanged,
                            label = stringResource(R.string.explore_budget_stops_label),
                            modifier = Modifier.weight(1f),
                            supporting = {
                                Text(text = stringResource(R.string.explore_budget_stops_support))
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                        )
                    }

                    if (!lowBalanceMessage.isNullOrBlank()) {
                        AppStateMessage(
                            title = stringResource(R.string.explore_budget_low_balance_title),
                            message = lowBalanceMessage,
                        )
                    } else if (!planErrorMessage.isNullOrBlank()) {
                        AppStateMessage(
                            title = stringResource(R.string.explore_retry),
                            message = planErrorMessage,
                        )
                    }

                    AppPrimaryButton(
                        text = stringResource(R.string.explore_budget_action),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isPlanLoading,
                        onClick = onBuildPlan,
                    )
                }
            }
        }
    }
}

@Composable
private fun ExploreBudgetPlanItemCard(
    stopOrder: Int,
    title: String,
    subtitle: String,
    estimatedCost: Long,
    experienceType: String,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onViewDetail: () -> Unit,
    onSendToChat: () -> Unit,
    onOpenMaps: () -> Unit,
) {
    AppSurfaceCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val cardShape = MaterialTheme.shapes.large
            val rowShape = remember(isExpanded) {
                if (isExpanded) {
                    RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                } else {
                    cardShape
                }
            }
            val verticalPadding = if (isExpanded) 12.dp else 16.dp

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(rowShape)
                    .clickable(onClick = onToggleExpanded)
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = verticalPadding),
                verticalAlignment = Alignment.Top,
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Text(
                        text = stopOrder.toString(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text(
                            text = title,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = stringResource(R.string.explore_budget_cost_format, formatSimpleAmount(estimatedCost)),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.End,
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = subtitle,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )

                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                        ) {
                            Text(
                                text = experienceType.replaceFirstChar {
                                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                                },
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        Icon(
                            painter = painterResource(
                                if (isExpanded) R.drawable.ic_expand_less_24 else R.drawable.ic_expand_more_24,
                            ),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = onViewDetail) {
                            Text(text = stringResource(R.string.explore_detail_title))
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = onOpenMaps) {
                            Text(text = stringResource(R.string.explore_budget_open_map_action))
                        }
                        TextButton(onClick = onSendToChat) {
                            Text(text = stringResource(R.string.explore_budget_send_to_chat_action))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExploreSearchBar(
    query: String,
    isAdvancedExpanded: Boolean,
    onQueryChange: (String) -> Unit,
    onToggleAdvanced: () -> Unit,
    onSearch: () -> Unit,
) {
    AppFormTextField(
        value = query,
        onValueChange = onQueryChange,
        label = stringResource(R.string.explore_search_action),
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(text = stringResource(R.string.explore_search_hint))
        },
        leading = {
            IconButton(onClick = onSearch) {
                Icon(
                    painter = painterResource(R.drawable.ic_search_24),
                    contentDescription = stringResource(R.string.explore_search_action),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        },
        trailing = {
            IconButton(onClick = onToggleAdvanced) {
                Icon(
                    painter = painterResource(
                        if (isAdvancedExpanded) R.drawable.ic_close_24 else R.drawable.ic_tune_24,
                    ),
                    contentDescription = stringResource(
                        if (isAdvancedExpanded) R.string.explore_filter_toggle_hide else R.string.explore_filter_toggle_show,
                    ),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
    )
}

@Composable
private fun ExploreTypeChipsRow(
    selectedType: ExplorePlaceType,
    nearMeSelected: Boolean,
    onTypeSelected: (ExplorePlaceType) -> Unit,
    onNearMeChanged: (Boolean) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(start = 2.dp, end = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        item {
            FilterChip(
                selected = selectedType == ExplorePlaceType.ALL,
                onClick = { onTypeSelected(ExplorePlaceType.ALL) },
                label = { Text(stringResource(R.string.explore_type_all)) },
                colors = FilterChipDefaults.filterChipColors(),
            )
        }
        item {
            FilterChip(
                selected = selectedType == ExplorePlaceType.FOOD,
                onClick = { onTypeSelected(ExplorePlaceType.FOOD) },
                label = { Text(stringResource(R.string.explore_type_food)) },
                colors = FilterChipDefaults.filterChipColors(),
            )
        }
        item {
            FilterChip(
                selected = selectedType == ExplorePlaceType.DRINK,
                onClick = { onTypeSelected(ExplorePlaceType.DRINK) },
                label = { Text(stringResource(R.string.explore_type_drink)) },
                colors = FilterChipDefaults.filterChipColors(),
            )
        }
        item {
            FilterChip(
                selected = nearMeSelected,
                onClick = { onNearMeChanged(!nearMeSelected) },
                label = { Text(stringResource(R.string.explore_filter_near_me)) },
                colors = FilterChipDefaults.filterChipColors(),
            )
        }
    }
}

@Composable
private fun ExploreAdvancedFilters(
    selectedProvinceQuery: String,
    areaOptions: List<String>,
    onProvinceSelected: (String) -> Unit,
    selectedMinRating: Int?,
    ratingOptions: List<String>,
    onMinRatingSelected: (Int) -> Unit,
    radiusKmInput: String,
    onRadiusChange: (String) -> Unit,
    onRandom: () -> Unit,
) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        DropdownSelector(
            label = stringResource(R.string.explore_area_hint),
            value = provinceLabelForQuery(selectedProvinceQuery, context),
            options = areaOptions,
            onSelected = onProvinceSelected,
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DropdownSelectorByIndex(
                label = stringResource(R.string.explore_min_rating_hint),
                value = ratingLabel(selectedMinRating, context),
                options = ratingOptions,
                onSelectedIndex = onMinRatingSelected,
                modifier = Modifier.weight(1f),
            )

            AppFormTextField(
                value = radiusKmInput,
                onValueChange = onRadiusChange,
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.explore_radius_hint),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            )
        }

        AppPrimaryButton(
            text = stringResource(R.string.explore_random_action),
            modifier = Modifier.fillMaxWidth(),
            onClick = onRandom,
        )
    }
}

@Composable
private fun DropdownSelector(
    label: String,
    value: String,
    options: List<String>,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Box(modifier = modifier) {
        AppFormTextField(
            value = value,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth(),
            label = label,
            readOnly = true,
            trailing = {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_expand_more_24),
                        contentDescription = null,
                    )
                }
            },
        )

        androidx.compose.material3.DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(),
        ) {
            options.forEach { option ->
                androidx.compose.material3.DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun DropdownSelectorByIndex(
    label: String,
    value: String,
    options: List<String>,
    onSelectedIndex: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Box(modifier = modifier) {
        AppFormTextField(
            value = value,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            label = label,
            readOnly = true,
            trailing = {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_expand_more_24),
                        contentDescription = null,
                    )
                }
            },
        )

        androidx.compose.material3.DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(),
        ) {
            options.forEachIndexed { index, option ->
                androidx.compose.material3.DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    onClick = {
                        onSelectedIndex(index)
                        expanded = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaceDetailBottomSheet(
    place: Place,
    accessToken: String,
    currentLocationLat: Double?,
    currentLocationLng: Double?,
    isFavorite: Boolean,
    favoriteToggleInProgress: Boolean,
    onToggleFavorite: () -> Unit,
    onRecordView: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(place.id, accessToken) {
        onRecordView()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close_24),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    text = stringResource(R.string.explore_detail_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )

                IconButton(
                    onClick = onToggleFavorite,
                    enabled = accessToken.isNotBlank() && !favoriteToggleInProgress,
                ) {
                    Icon(
                        painter = painterResource(
                            if (isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline,
                        ),
                        contentDescription = stringResource(R.string.explore_toggle_favorite),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(22.dp)),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.tertiaryContainer),
                    )

                    Icon(
                        painter = painterResource(R.drawable.ic_restaurant_24),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(48.dp),
                    )

                    AsyncImage(
                        model = place.imageUrl?.takeIf { it.isNotBlank() },
                        contentDescription = valueOrUpdating(place.name, context),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
            }

            Text(
                text = valueOrUpdating(place.name, context),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            val location = normalizedValue(place.province)
                ?: normalizedValue(place.district)
                ?: context.getString(R.string.explore_updating)

            Text(
                text = stringResource(
                    R.string.explore_detail_meta_format,
                    valueOrUpdating(place.effectiveTag, context),
                    location,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BadgePill(text = stringResource(R.string.explore_detail_opening_badge_format, valueOrUpdating(place.openHours, context)))
                val formattedPrice = formatPriceRange(place.priceRange)
                BadgePill(text = if (formattedPrice != null) "Giá: $formattedPrice" else stringResource(R.string.explore_detail_price_badge_format, context.getString(R.string.explore_updating)))
            }

            BadgePill(
                text = stringResource(
                    R.string.explore_detail_rating_badge_format,
                    ratingOrUpdating(place.rating, place.reviewCount, context),
                ),
            )

            val mapsUrl = resolveGoogleMapsUrl(place, currentLocationLat, currentLocationLng)
            Button(
                onClick = {
                    if (place.lat == null || place.lng == null) {
                        if (mapsUrl == null) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.explore_detail_no_map_data),
                                Toast.LENGTH_SHORT,
                            ).show()
                            return@Button
                        }
                        openGoogleMaps(context, mapsUrl)
                        return@Button
                    }

                    val didOpenNavigation = openGoogleMapsByCoordinates(
                        context = context,
                        placeName = place.name,
                        lat = place.lat,
                        lng = place.lng,
                    )
                    if (!didOpenNavigation && mapsUrl == null) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.explore_detail_no_map_data),
                            Toast.LENGTH_SHORT,
                        ).show()
                        return@Button
                    }
                    if (!didOpenNavigation && mapsUrl != null) {
                        openGoogleMaps(context, mapsUrl)
                    }
                },
                enabled = (place.lat != null && place.lng != null) || mapsUrl != null,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(text = stringResource(R.string.explore_detail_direction))
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.explore_detail_summary_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = buildOverviewText(place, context),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.explore_detail_address_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = buildAddressText(place, context),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            val coordinatesValue = coordinatesOrUpdating(place.lat, place.lng, context)
            Text(
                text = detailLine(R.string.explore_detail_coordinates, coordinatesValue, context),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            val currentLocationValue = coordinatesOrUpdating(currentLocationLat, currentLocationLng, context)
            Text(
                text = detailLine(R.string.explore_detail_current_location, currentLocationValue, context),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun BadgePill(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun buildAreaDisplayOptions(context: Context): List<String> {
    return buildList {
        add(context.getString(R.string.explore_all_areas_option))
        predefinedAreaOptions.forEach { option ->
            add(context.getString(option.labelResId))
        }
    }
}

private fun normalizeProvinceSelection(raw: String, context: Context): String {
    val value = raw.trim()
    if (value.isBlank()) {
        return ""
    }
    if (value == context.getString(R.string.explore_all_areas_option)) {
        return ""
    }

    val normalizedSelection = value.lowercase(Locale.ROOT)
    return predefinedAreaOptions.firstOrNull { context.getString(it.labelResId) == value }?.query
        ?: normalizedSelection
}

private fun provinceLabelForQuery(query: String, context: Context): String {
    if (query.isBlank()) {
        return context.getString(R.string.explore_all_areas_option)
    }

    val normalizedQuery = query.lowercase(Locale.ROOT)
    return predefinedAreaOptions.firstOrNull { it.query == normalizedQuery }
        ?.let { context.getString(it.labelResId) }
        ?: query
}

private fun buildRatingOptions(context: Context): List<String> {
    return listOf(
        context.getString(R.string.explore_rating_all_option),
        context.getString(R.string.explore_rating_1_option),
        context.getString(R.string.explore_rating_2_option),
        context.getString(R.string.explore_rating_3_option),
        context.getString(R.string.explore_rating_4_option),
        context.getString(R.string.explore_rating_5_option),
    )
}

private fun minRatingFromPosition(position: Int): Int? {
    return when (position) {
        0 -> null
        1 -> 1
        2 -> 2
        3 -> 3
        4 -> 4
        5 -> 5
        else -> null
    }
}

private fun ratingLabel(minRating: Int?, context: Context): String {
    return when (minRating) {
        1 -> context.getString(R.string.explore_rating_1_option)
        2 -> context.getString(R.string.explore_rating_2_option)
        3 -> context.getString(R.string.explore_rating_3_option)
        4 -> context.getString(R.string.explore_rating_4_option)
        5 -> context.getString(R.string.explore_rating_5_option)
        else -> context.getString(R.string.explore_rating_all_option)
    }
}

private fun detailLine(@StringRes labelRes: Int, value: String, context: Context): String {
    return context.getString(R.string.explore_detail_line_format, context.getString(labelRes), value)
}

private fun buildOverviewText(place: Place, context: Context): String {
    val lines = mutableListOf<String>()

    normalizedValue(place.effectiveTag)?.let {
        lines.add(detailLine(R.string.explore_detail_effective_tag, it, context))
    }
    normalizedValue(place.category)?.let {
        lines.add(detailLine(R.string.explore_detail_category, it, context))
    }
    normalizedValue(place.mealType)?.let {
        lines.add(detailLine(R.string.explore_detail_meal_type, it, context))
    }
    normalizedValue(place.priceRange)?.let { raw ->
        val formatted = formatPriceRange(raw) ?: raw
        lines.add(detailLine(R.string.explore_detail_price_range, formatted, context))
    }
    normalizedValue(place.imageUrl)?.let {
        lines.add(detailLine(R.string.explore_detail_image_url, context.getString(R.string.explore_detail_available), context))
    }

    return if (lines.isEmpty()) {
        context.getString(R.string.explore_detail_summary_empty)
    } else {
        lines.joinToString("\n")
    }
}

private fun buildAddressText(place: Place, context: Context): String {
    val parts = listOf(
        normalizedValue(place.address),
        normalizedValue(place.district),
        normalizedValue(place.province),
    ).filterNotNull()

    return if (parts.isEmpty()) {
        context.getString(R.string.explore_updating)
    } else {
        parts.joinToString(", ")
    }
}

private fun normalizedValue(value: String?): String? {
    return value?.trim()?.takeIf { it.isNotBlank() }
}

private fun valueOrUpdating(value: String?, context: Context): String {
    return normalizedValue(value) ?: context.getString(R.string.explore_updating)
}

private fun formatPriceRange(raw: String?): String? {
    val input = raw?.trim()?.takeIf { it.isNotBlank() } ?: return null
    val normalized = input
        .replace("VNĐ", "VND", ignoreCase = true)
        .replace("đ", " VND", ignoreCase = true)
        .replace("dong", " VND", ignoreCase = true)
        .replace("nguời", "nguoi", ignoreCase = true)
        .replace("người", "nguoi", ignoreCase = true)

    val values = Regex("""(\d+(?:[\.,]\d+)?)\s*(tr|m|k|nghin|ngan|vnd)?""", RegexOption.IGNORE_CASE)
        .findAll(normalized)
        .mapNotNull { match ->
            val number = match.groupValues[1]
            val unit = match.groupValues[2].lowercase(Locale.ROOT)
            parsePriceAmount(number, unit)
        }
        .toList()

    if (values.isEmpty()) {
        return input
    }

    val perPersonCount = Regex("""(\d+)\s*nguoi""", RegexOption.IGNORE_CASE)
        .find(normalized)
        ?.groupValues
        ?.getOrNull(1)
        ?.toIntOrNull()
        ?.takeIf { it > 1 }

    val normalizedValues = if (perPersonCount != null && values.size == 1) {
        listOf(values.first() / perPersonCount)
    } else {
        values
    }

    return when (normalizedValues.size) {
        1 -> "${formatSimpleAmount(normalizedValues.first())} đồng"
        else -> "${formatSimpleAmount(normalizedValues.min())} - ${formatSimpleAmount(normalizedValues.max())} đồng"
    }
}

private fun parsePriceAmount(number: String, unit: String): Long? {
    val compact = number.trim().replace(" ", "")
    val normalizedUnit = unit.trim().lowercase(Locale.ROOT)
    return runCatching {
        when {
            normalizedUnit == "k" || normalizedUnit == "nghin" || normalizedUnit == "ngan" -> {
                compact.replace(",", ".").toDouble().times(1_000).toLong()
            }
            normalizedUnit == "tr" || normalizedUnit == "m" -> {
                compact.replace(",", ".").toDouble().times(1_000_000).toLong()
            }
            normalizedUnit == "vnd" -> compact.replace(Regex("[.,]"), "").toLong()
            compact.contains('.') || compact.contains(',') -> compact.replace(Regex("[.,]"), "").toLong()
            else -> {
                val parsed = compact.toLong()
                if (parsed < 1_000L) parsed * 1_000L else parsed
            }
        }
    }.getOrNull()
}

private fun ratingOrUpdating(rating: Double?, reviewCount: Int?, context: Context): String {
    if (rating == null || reviewCount == null) {
        return context.getString(R.string.explore_updating)
    }
    return context.getString(R.string.explore_rating_format, rating, reviewCount)
}

private fun coordinatesOrUpdating(lat: Double?, lng: Double?, context: Context): String {
    if (lat == null || lng == null) {
        return context.getString(R.string.explore_updating)
    }
    return context.getString(R.string.explore_coordinates_format, lat, lng)
}

private fun resolveGoogleMapsUrl(place: Place, currentLat: Double?, currentLng: Double?): String? {
    val origin = if (currentLat != null && currentLng != null) {
        "$currentLat,$currentLng"
    } else {
        null
    }

    if (place.lat != null && place.lng != null) {
        return if (origin != null) {
            "https://www.google.com/maps/dir/?api=1&origin=${Uri.encode(origin)}&destination=${place.lat},${place.lng}&travelmode=driving"
        } else {
            "https://www.google.com/maps/search/?api=1&query=${place.lat},${place.lng}"
        }
    }

    val directUrl = normalizedValue(place.googleMapsUrl)
    if (directUrl != null && origin == null) {
        return directUrl
    }

    val query = listOf(
        normalizedValue(place.name),
        normalizedValue(place.address),
        normalizedValue(place.district),
        normalizedValue(place.province),
    ).filterNotNull().joinToString(" ")

    if (query.isBlank()) {
        return null
    }

    return if (origin != null) {
        "https://www.google.com/maps/dir/?api=1&origin=${Uri.encode(origin)}&destination=${Uri.encode(query)}&travelmode=driving"
    } else {
        "https://www.google.com/maps/search/?api=1&query=${Uri.encode(query)}"
    }
}

private fun openGoogleMaps(context: Context, url: String) {
    val uri = Uri.parse(url)

    val mapsAppIntent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.google.android.apps.maps")
    }

    val fallbackIntent = Intent(Intent.ACTION_VIEW, uri)

    if (tryStartActivity(context, mapsAppIntent)) return
    if (tryStartActivity(context, fallbackIntent)) return

    Toast.makeText(
        context,
        context.getString(R.string.explore_detail_no_map_app),
        Toast.LENGTH_SHORT,
    ).show()
}

private fun tryStartActivity(context: Context, intent: Intent): Boolean {
    return try {
        context.startActivity(intent)
        true
    } catch (_: ActivityNotFoundException) {
        false
    } catch (_: SecurityException) {
        false
    }
}

private fun openGoogleMapsByCoordinates(
    context: Context,
    placeName: String?,
    lat: Double,
    lng: Double,
): Boolean {
    val encodedLabel = Uri.encode(placeName?.trim().orEmpty())
    val query = if (encodedLabel.isBlank()) "$lat,$lng" else "$lat,$lng($encodedLabel)"

    val navigationIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("google.navigation:q=$query"),
    ).apply {
        setPackage("com.google.android.apps.maps")
    }

    if (tryStartActivity(context, navigationIntent)) return true

    val geoIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("geo:0,0?q=$query"),
    )
    if (tryStartActivity(context, geoIntent)) return true

    return false
}

private suspend fun observePagingLoadMore(
    listState: LazyListState,
    onLoadMore: () -> Unit,
) {
    snapshotFlow {
        val layoutInfo = listState.layoutInfo
        val totalItems = layoutInfo.totalItemsCount
        val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        lastVisibleItem to totalItems
    }
        .distinctUntilChanged()
        .filter { (_, total) -> total > 0 }
        .map { (lastVisible, total) -> lastVisible >= total - LOAD_MORE_THRESHOLD }
        .distinctUntilChanged()
        .filter { it }
        .collectLatest {
            onLoadMore()
        }
}
