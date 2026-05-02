package com.example.mobileproject.presentation.ui.screen.home

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.mobileproject.presentation.ui.icons.LucideHeart
import com.example.mobileproject.presentation.ui.icons.LucideMapPin
import com.example.mobileproject.presentation.ui.icons.LucideUser
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.mobileproject.BuildConfig
import com.example.mobileproject.R
import com.example.mobileproject.data.datasource.remote.ApiService
import com.example.mobileproject.presentation.service.MapShareForegroundService
import com.example.mobileproject.presentation.ui.components.core.AppMetricChip
import com.example.mobileproject.presentation.ui.components.core.AppPrimaryButton
import com.example.mobileproject.presentation.ui.components.core.AppScreenBackground
import com.example.mobileproject.presentation.ui.components.core.AppSectionHeader
import com.example.mobileproject.presentation.ui.components.core.AppSurfaceCard
import com.example.mobileproject.presentation.ui.screen.couple.CoupleConnectActivity
import com.example.mobileproject.presentation.ui.screen.map.MapShareActivity
import com.example.mobileproject.presentation.viewmodel.CoupleUiState
import com.example.mobileproject.presentation.viewmodel.CoupleViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit

private const val DEFAULT_ZOOM: Double = 17.0
private const val MAP_BOOTSTRAP_THROTTLE_MS: Long = 10_000L

@Composable
fun HomeScreen(
    accessToken: String,
    apiService: ApiService,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val coupleViewModel: CoupleViewModel = hiltViewModel()
    val coupleState by coupleViewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    var currentCoupleId by remember { mutableStateOf<String?>(null) }

    var mapView by remember { mutableStateOf<MapView?>(null) }

    fun createMapViewIfNeeded(): MapView {
        mapView?.let { existing ->
            return existing
        }

        return MapView(context).apply {
            Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(DEFAULT_ZOOM)
        }.also { created ->
            mapView = created
        }
    }

    var myMarker by remember { mutableStateOf<Marker?>(null) }
    var partnerMarker by remember { mutableStateOf<Marker?>(null) }
    var hasCenteredOnce by remember { mutableStateOf(false) }

    var mapBootstrapInFlight by remember { mutableStateOf(false) }
    var lastMapBootstrapAttemptMs by remember { mutableStateOf(0L) }
    var serviceStartedForCoupleId by remember { mutableStateOf<String?>(null) }

    var hasRequestedSharingPermissions by remember { mutableStateOf(false) }
    var pendingSharingPermissionRequest by remember { mutableStateOf<List<String>>(emptyList()) }

    fun updateMarker(
        existing: Marker?,
        titleRes: Int,
        lat: Double,
        lng: Double,
        onCreated: (Marker) -> Unit,
    ): Marker? {
        val activeMapView = mapView ?: return existing
        val point = GeoPoint(lat, lng)
        val marker = existing ?: Marker(activeMapView).also {
            it.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            it.title = context.getString(titleRes)
            activeMapView.overlays.add(it)
            onCreated(it)
        }
        marker.position = point
        activeMapView.invalidate()
        return marker
    }

    fun centerOn(point: GeoPoint) {
        val activeMapView = mapView ?: return
        hasCenteredOnce = true
        activeMapView.controller.setZoom(DEFAULT_ZOOM)
        activeMapView.controller.setCenter(point)
    }

    fun centerIfNeeded(lat: Double, lng: Double) {
        val activeMapView = mapView ?: return
        if (hasCenteredOnce) {
            return
        }
        hasCenteredOnce = true
        activeMapView.controller.setZoom(DEFAULT_ZOOM)
        activeMapView.controller.setCenter(GeoPoint(lat, lng))
    }

    fun missingSharingPermissions(): List<String> {
        val required = buildList {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        return required.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }
    }

    fun maybeStartSharingService() {
        val coupleId = currentCoupleId?.trim().orEmpty()
        if (accessToken.isBlank() || coupleId.isBlank()) {
            return
        }
        if (serviceStartedForCoupleId == coupleId) {
            return
        }

        val missing = missingSharingPermissions()
        if (missing.isNotEmpty()) {
            if (!hasRequestedSharingPermissions) {
                hasRequestedSharingPermissions = true
                pendingSharingPermissionRequest = missing
            }
            return
        }

        MapShareForegroundService.start(context, accessToken, coupleId)
        serviceStartedForCoupleId = coupleId
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        pendingSharingPermissionRequest = emptyList()
        val grantedAll = grants.values.all { it }
        if (!grantedAll) {
            Toast.makeText(context, context.getString(R.string.map_share_permission_denied), Toast.LENGTH_SHORT).show()
            return@rememberLauncherForActivityResult
        }

        maybeStartSharingService()
    }

    LaunchedEffect(pendingSharingPermissionRequest) {
        if (pendingSharingPermissionRequest.isNotEmpty()) {
            permissionLauncher.launch(pendingSharingPermissionRequest.toTypedArray())
        }
    }

    val openFullScreenMap by rememberUpdatedState {
        if (accessToken.isBlank()) {
            return@rememberUpdatedState
        }

        val intent = Intent(context, MapShareActivity::class.java)
            .putExtra(MapShareActivity.EXTRA_ACCESS_TOKEN, accessToken)
            .putExtra(MapShareActivity.EXTRA_KEEP_SHARING_ACTIVE, true)

        val coupleId = currentCoupleId?.trim().orEmpty()
        if (coupleId.isNotBlank()) {
            intent.putExtra(MapShareActivity.EXTRA_COUPLE_ID, coupleId)
        }

        context.startActivity(intent)
    }

    val gestureDetector = remember {
        GestureDetector(
            context,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    openFullScreenMap()
                    return true
                }
            },
        )
    }

    DisposableEffect(mapView, gestureDetector) {
        val activeMapView = mapView
        if (activeMapView == null) {
            return@DisposableEffect onDispose { }
        }

        activeMapView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            false
        }

        onDispose {
            activeMapView.setOnTouchListener(null)
        }
    }

    val mapUpdatesReceiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent == null) {
                    return
                }

                when (intent.action) {
                    MapShareForegroundService.ACTION_MY_LOCATION -> {
                        val lat = intent.getDoubleExtra(MapShareForegroundService.EXTRA_LATITUDE, Double.NaN)
                        val lng = intent.getDoubleExtra(MapShareForegroundService.EXTRA_LONGITUDE, Double.NaN)
                        if (!lat.isNaN() && !lng.isNaN()) {
                            myMarker = updateMarker(
                                existing = myMarker,
                                titleRes = R.string.map_share_me_marker,
                                lat = lat,
                                lng = lng,
                                onCreated = { created -> myMarker = created },
                            )
                            centerIfNeeded(lat, lng)
                        }
                    }

                    MapShareForegroundService.ACTION_PARTNER_LOCATION -> {
                        val lat = intent.getDoubleExtra(MapShareForegroundService.EXTRA_LATITUDE, Double.NaN)
                        val lng = intent.getDoubleExtra(MapShareForegroundService.EXTRA_LONGITUDE, Double.NaN)
                        if (!lat.isNaN() && !lng.isNaN()) {
                            partnerMarker = updateMarker(
                                existing = partnerMarker,
                                titleRes = R.string.map_share_partner_marker,
                                lat = lat,
                                lng = lng,
                                onCreated = { created -> partnerMarker = created },
                            )
                            if (!hasCenteredOnce) {
                                centerIfNeeded(lat, lng)
                            }
                        }
                    }
                }
            }
        }
    }

    DisposableEffect(coupleState.paired) {
        if (!coupleState.paired) {
            return@DisposableEffect onDispose { }
        }

        val filter = IntentFilter().apply {
            addAction(MapShareForegroundService.ACTION_MY_LOCATION)
            addAction(MapShareForegroundService.ACTION_PARTNER_LOCATION)
        }

        ContextCompat.registerReceiver(
            context,
            mapUpdatesReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )

        onDispose {
            runCatching { context.unregisterReceiver(mapUpdatesReceiver) }
        }
    }

    DisposableEffect(lifecycleOwner, mapView) {
        val activeMapView = mapView
        if (activeMapView == null) {
            return@DisposableEffect onDispose { }
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> activeMapView.onResume()
                Lifecycle.Event.ON_PAUSE -> activeMapView.onPause()
                Lifecycle.Event.ON_DESTROY -> runCatching { activeMapView.onDetach() }
                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(coupleState.paired) {
        if (coupleState.paired) {
            createMapViewIfNeeded()
        } else {
            myMarker = null
            partnerMarker = null
            hasCenteredOnce = false
            mapView?.let { activeMapView ->
                runCatching {
                    activeMapView.onPause()
                    activeMapView.onDetach()
                }
            }
            mapView = null
        }
    }

    LaunchedEffect(accessToken) {
        if (accessToken.isNotBlank()) {
            coupleViewModel.loadStatus(accessToken)
        }
    }

    LaunchedEffect(coupleState.paired, accessToken) {
        if (!coupleState.paired || accessToken.isBlank()) {
            return@LaunchedEffect
        }

        createMapViewIfNeeded()

        val now = System.currentTimeMillis()
        if (mapBootstrapInFlight) {
            return@LaunchedEffect
        }
        if (now - lastMapBootstrapAttemptMs < MAP_BOOTSTRAP_THROTTLE_MS) {
            return@LaunchedEffect
        }

        lastMapBootstrapAttemptMs = now
        mapBootstrapInFlight = true

        try {
            val tokenHeader = "Bearer ${accessToken.trim()}"
            val response = runCatching { apiService.getMapLastLocations(tokenHeader) }.getOrNull()
            val body = response?.body()
            if (response == null || !response.isSuccessful || body == null || !body.success) {
                return@LaunchedEffect
            }

            val resolvedCoupleId = body.coupleId?.trim().orEmpty()
            if (resolvedCoupleId.isNotBlank()) {
                currentCoupleId = resolvedCoupleId
            }

            body.myLocation?.let { loc ->
                val lat = loc.latitude
                val lng = loc.longitude
                if (lat != null && lng != null) {
                    myMarker = updateMarker(
                        existing = myMarker,
                        titleRes = R.string.map_share_me_marker,
                        lat = lat,
                        lng = lng,
                        onCreated = { created -> myMarker = created },
                    )
                    centerIfNeeded(lat, lng)
                }
            }

            body.partnerLocation?.let { loc ->
                val lat = loc.latitude
                val lng = loc.longitude
                if (lat != null && lng != null) {
                    partnerMarker = updateMarker(
                        existing = partnerMarker,
                        titleRes = R.string.map_share_partner_marker,
                        lat = lat,
                        lng = lng,
                        onCreated = { created -> partnerMarker = created },
                    )
                    if (!hasCenteredOnce) {
                        centerIfNeeded(lat, lng)
                    }
                }
            }

            maybeStartSharingService()
        } finally {
            mapBootstrapInFlight = false
        }
    }

    LaunchedEffect(coupleState.paired) {
        if (!coupleState.paired && serviceStartedForCoupleId != null) {
            MapShareForegroundService.stop(context)
            serviceStartedForCoupleId = null
        }
    }

    HomeContent(
        state = coupleState,
        accessToken = accessToken,
        onPairNow = {
            if (accessToken.isBlank()) {
                return@HomeContent
            }
            context.startActivity(
                Intent(context, CoupleConnectActivity::class.java)
                    .putExtra(CoupleConnectActivity.EXTRA_ACCESS_TOKEN, accessToken),
            )
        },
        daysTogether = coupleState.daysTogether?.coerceAtLeast(1)
            ?: computeDaysTogetherFromStartAt(coupleState.startAt)
            ?: 1L,
        onCenterMe = {
            val point = myMarker?.position ?: return@HomeContent
            centerOn(point)
        },
        onCenterPartner = {
            val point = partnerMarker?.position ?: return@HomeContent
            centerOn(point)
        },
        mapView = mapView,
    )
}

@Composable
private fun HomeContent(
    state: CoupleUiState,
    accessToken: String,
    daysTogether: Long,
    onPairNow: () -> Unit,
    onCenterMe: () -> Unit,
    onCenterPartner: () -> Unit,
    mapView: MapView?,
) {
    var revealStep by remember { mutableStateOf(0) }

    LaunchedEffect(state.paired, state.isLoading) {
        revealStep = 0
        delay(60)
        revealStep = 1
        delay(80)
        revealStep = 2
        delay(90)
        revealStep = 3
        if (state.paired) {
            delay(90)
            revealStep = 4
        }
    }

    val pairStatus = when {
        accessToken.isBlank() -> "Session off"
        state.paired -> "Connected"
        state.isLoading -> "Syncing"
        else -> "Pending"
    }
    val codeValue = state.myCoupleCode?.takeIf { it.isNotBlank() } ?: "--"

    AppScreenBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppSectionHeader(
                title = stringResource(R.string.page_home),
                subtitle = if (state.paired) {
                    stringResource(R.string.home_pair_connected_subtitle)
                } else {
                    stringResource(R.string.home_pair_subtitle)
                },
            )

            AnimatedVisibility(visible = revealStep >= 1) {
                AppSurfaceCard(
                    modifier = Modifier.animateContentSize(),
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            AppMetricChip(
                                value = daysTogether.toString(),
                                label = stringResource(R.string.home_days_together_label),
                                modifier = Modifier.weight(1f),
                            )
                            AppMetricChip(
                                value = pairStatus,
                                label = stringResource(R.string.home_pair_title),
                                modifier = Modifier.weight(1f),
                            )
                            AppMetricChip(
                                value = codeValue,
                                label = "Pair code",
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(visible = revealStep >= 2) {
                AppSurfaceCard(
                    modifier = Modifier.animateContentSize(),
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)) {
                        PairSection(
                            state = state,
                            accessToken = accessToken,
                            onPairNow = onPairNow,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            if (state.paired) {
                AnimatedVisibility(visible = revealStep >= 3) {
                    DaysTogetherCard(
                        daysTogether = daysTogether,
                        showAnniversaryHint = state.anniversaryTomorrow == true,
                    )
                }

                AnimatedVisibility(visible = revealStep >= 4) {
                    AppSurfaceCard(
                        modifier = Modifier.animateContentSize(),
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            AppSectionHeader(
                                title = stringResource(R.string.map_share_title),
                                subtitle = stringResource(R.string.home_pair_connected_note),
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp)
                                    .clip(RoundedCornerShape(18.dp)),
                            ) {
                                val activeMapView = mapView
                                if (activeMapView != null) {
                                    AndroidView(
                                        factory = { activeMapView },
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = stringResource(R.string.home_pair_loading_button),
                                            color = colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }

                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    SmallFloatingActionButton(
                                        onClick = onCenterPartner,
                                        containerColor = colorScheme.surface,
                                        contentColor = colorScheme.primary,
                                    ) {
                                        Icon(
                                            imageVector = LucideUser,
                                            contentDescription = stringResource(R.string.map_share_partner_marker),
                                        )
                                    }

                                    SmallFloatingActionButton(
                                        onClick = onCenterMe,
                                        containerColor = colorScheme.surface,
                                        contentColor = colorScheme.primary,
                                    ) {
                                        Icon(
                                            imageVector = LucideMapPin,
                                            contentDescription = stringResource(R.string.map_share_me_marker),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PairSection(
    state: CoupleUiState,
    accessToken: String,
    onPairNow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val noSession = accessToken.isBlank()

    val titleText: String
    val subtitleText: String
    val showConnectedCard: Boolean
    val showPairButton: Boolean
    val pairButtonEnabled: Boolean
    val pairButtonText: String

    if (noSession) {
        titleText = stringResource(R.string.home_pair_title)
        subtitleText = stringResource(R.string.home_pair_subtitle)
        showConnectedCard = false
        showPairButton = true
        pairButtonEnabled = false
        pairButtonText = stringResource(R.string.home_pair_button)
    } else if (state.paired) {
        titleText = stringResource(R.string.home_pair_connected_title)
        subtitleText = stringResource(R.string.home_pair_connected_subtitle)
        showConnectedCard = true
        showPairButton = false
        pairButtonEnabled = false
        pairButtonText = stringResource(R.string.home_pair_button)
    } else {
        titleText = stringResource(R.string.home_pair_title)
        subtitleText = stringResource(R.string.home_pair_subtitle)
        showConnectedCard = false
        showPairButton = true
        pairButtonEnabled = !state.isLoading
        pairButtonText = if (state.isLoading) {
            stringResource(R.string.home_pair_loading_button)
        } else {
            stringResource(R.string.home_pair_button)
        }
    }

    Column(modifier = modifier) {
        Text(
            text = titleText,
            color = colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitleText,
            color = colorScheme.onSurfaceVariant,
        )

        if (!noSession && !state.paired) {
            val myCode = state.myCoupleCode
            if (!myCode.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.home_pair_my_code, myCode),
                    color = colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        if (showConnectedCard) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = colorScheme.primaryContainer),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = stringResource(R.string.home_pair_connected_badge),
                        color = colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    val partnerDisplayName = state.partnerUsername ?: stringResource(R.string.home_pair_partner_unknown)
                    Text(
                        text = stringResource(R.string.home_pair_partner_name, partnerDisplayName),
                        color = colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.home_pair_connected_note),
                        color = colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        val hintText = when {
            noSession -> stringResource(R.string.home_pair_session_expired)
            !state.errorMessage.isNullOrBlank() -> state.errorMessage
            state.outgoingStatus.equals("PENDING", ignoreCase = true) -> stringResource(R.string.home_pair_outgoing_pending)
            state.outgoingStatus.equals("REJECTED", ignoreCase = true) -> stringResource(R.string.home_pair_outgoing_rejected)
            !state.myCoupleCodeExpiresAt.isNullOrBlank() -> stringResource(
                R.string.home_pair_code_expiry,
                state.myCoupleCodeExpiresAt ?: "",
            )
            else -> null
        }

        if (!hintText.isNullOrBlank() && !state.paired) {
            Spacer(modifier = Modifier.height(10.dp))
            val hintColor = if (
                noSession ||
                !state.errorMessage.isNullOrBlank() ||
                state.outgoingStatus.equals("REJECTED", ignoreCase = true)
            ) {
                colorScheme.primary
            } else {
                colorScheme.onSurfaceVariant
            }

            Text(
                text = hintText,
                color = hintColor,
            )
        }

        if (showPairButton) {
            Spacer(modifier = Modifier.height(12.dp))
            AppPrimaryButton(
                text = pairButtonText,
                onClick = onPairNow,
                enabled = pairButtonEnabled,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun DaysTogetherCard(
    daysTogether: Long,
    showAnniversaryHint: Boolean,
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = daysTogether.toString(),
                style = MaterialTheme.typography.displayMedium,
                color = colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = stringResource(R.string.home_days_together_label),
                color = colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AvatarChip(containerColor = colorScheme.primaryContainer)
                Spacer(modifier = Modifier.width(12.dp))
                AvatarChip(
                    containerColor = colorScheme.surfaceVariant,
                    icon = LucideHeart,
                    iconTint = colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(12.dp))
                AvatarChip(containerColor = colorScheme.primaryContainer)
            }

            if (showAnniversaryHint) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = colorScheme.primaryContainer),
                    shape = RoundedCornerShape(999.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Text(
                        text = stringResource(R.string.home_days_anniversary_tomorrow),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun AvatarChip(
    containerColor: Color,
    icon: ImageVector = LucideUser,
    iconTint: Color = MaterialTheme.colorScheme.onPrimaryContainer,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = CircleShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.size(44.dp),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector = icon,
                contentDescription = stringResource(R.string.cd_avatar),
                tint = iconTint,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

private fun computeDaysTogetherFromStartAt(startAt: String?): Long? {
    val datePart = startAt?.takeIf { it.length >= 10 }?.substring(0, 10) ?: return null
    val parts = datePart.split("-")
    if (parts.size != 3) {
        return null
    }

    val year = parts[0].toIntOrNull() ?: return null
    val month = parts[1].toIntOrNull() ?: return null
    val day = parts[2].toIntOrNull() ?: return null

    val utc = TimeZone.getTimeZone("UTC")

    val startCal = GregorianCalendar(utc).apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month - 1)
        set(Calendar.DAY_OF_MONTH, day)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val todayCal = GregorianCalendar(utc).apply {
        timeInMillis = System.currentTimeMillis()
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val diffMillis = todayCal.timeInMillis - startCal.timeInMillis
    val days = TimeUnit.MILLISECONDS.toDays(diffMillis) + 1
    return days.coerceAtLeast(1)
}
