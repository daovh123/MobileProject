package com.example.mobileproject.presentation.ui.screen.map

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.mobileproject.BuildConfig
import com.example.mobileproject.R
import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.data.datasource.remote.ApiService
import com.example.mobileproject.presentation.service.MapShareForegroundService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import javax.inject.Inject

@AndroidEntryPoint
class MapShareActivity : AppCompatActivity() {

    @Inject
    lateinit var authSessionStore: AuthSessionStore

    @Inject
    lateinit var apiService: ApiService

    private var mapView: MapView? = null
    private var pendingStartAfterPermissions: Boolean = false

    private var accessToken: String = ""
    private var coupleId: String? = null
    private var keepSharingActive: Boolean = false

    private var myMarker: Marker? = null
    private var partnerMarker: Marker? = null

    private var hasCenteredOnce: Boolean = false
    private var receiverRegistered: Boolean = false

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val grantedAll = grants.values.all { it }
        if (!grantedAll) {
            Toast.makeText(this, getString(R.string.map_share_permission_denied), Toast.LENGTH_SHORT).show()
            finish()
            return@registerForActivityResult
        }

        loadInitialStateAndStart()
    }

    private val mapUpdatesReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) {
                return
            }

            when (intent.action) {
                MapShareForegroundService.ACTION_MY_LOCATION -> {
                    val lat = intent.getDoubleExtra(MapShareForegroundService.EXTRA_LATITUDE, Double.NaN)
                    val lng = intent.getDoubleExtra(MapShareForegroundService.EXTRA_LONGITUDE, Double.NaN)
                    if (!lat.isNaN() && !lng.isNaN()) {
                        updateMyMarker(lat, lng)
                        centerIfNeeded(lat, lng)
                    }
                }

                MapShareForegroundService.ACTION_PARTNER_LOCATION -> {
                    val lat = intent.getDoubleExtra(MapShareForegroundService.EXTRA_LATITUDE, Double.NaN)
                    val lng = intent.getDoubleExtra(MapShareForegroundService.EXTRA_LONGITUDE, Double.NaN)
                    if (!lat.isNaN() && !lng.isNaN()) {
                        updatePartnerMarker(lat, lng)
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        accessToken = intent.getStringExtra(EXTRA_ACCESS_TOKEN).orEmpty()
        keepSharingActive = intent.getBooleanExtra(EXTRA_KEEP_SHARING_ACTIVE, false)
        if (accessToken.isBlank()) {
            accessToken = authSessionStore.load()?.token.orEmpty()
        }
        coupleId = intent.getStringExtra(EXTRA_COUPLE_ID)

        setContent {
            MapShareScreen(
                onClose = { finish() },
                onMapReady = { createdMapView ->
                    mapView = createdMapView
                    initMap(createdMapView)

                    if (pendingStartAfterPermissions) {
                        pendingStartAfterPermissions = false
                        loadInitialStateAndStart()
                    }
                },
                onDisposeMap = {
                    mapView = null
                },
            )
        }

        if (accessToken.isBlank()) {
            Toast.makeText(this, getString(R.string.home_pair_session_expired), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        requestPermissionsIfNeeded()
    }

    private fun initMap(mapView: MapView) {
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID

        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(DEFAULT_ZOOM)
    }

    private fun requestPermissionsIfNeeded() {
        val requiredPermissions = buildList {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        val missingPermissions = requiredPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isEmpty()) {
            startWhenReady()
            return
        }

        permissionLauncher.launch(missingPermissions.toTypedArray())
    }

    private fun startWhenReady() {
        if (mapView == null) {
            pendingStartAfterPermissions = true
            return
        }

        loadInitialStateAndStart()
    }

    private fun loadInitialStateAndStart() {
        val mapView = mapView
        if (mapView == null) {
            pendingStartAfterPermissions = true
            return
        }

        registerForMapUpdatesIfNeeded()

        lifecycleScope.launch {
            val tokenHeader = "Bearer ${accessToken.trim()}"

            val response = runCatching { apiService.getMapLastLocations(tokenHeader) }.getOrNull()
            val body = response?.body()

            if (response == null || !response.isSuccessful || body == null || !body.success) {
                val message = body?.message?.takeIf { it.isNotBlank() }
                    ?: getString(R.string.map_share_load_failed)
                Toast.makeText(this@MapShareActivity, message, Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }

            val resolvedCoupleId = (coupleId?.takeIf { it.isNotBlank() } ?: body.coupleId)
                ?.trim()
                .orEmpty()

            if (resolvedCoupleId.isBlank()) {
                Toast.makeText(this@MapShareActivity, getString(R.string.map_share_load_failed), Toast.LENGTH_SHORT)
                    .show()
                finish()
                return@launch
            }

            coupleId = resolvedCoupleId

            body.myLocation?.let { loc ->
                val lat = loc.latitude
                val lng = loc.longitude
                if (lat != null && lng != null) {
                    updateMyMarker(lat, lng)
                    centerIfNeeded(lat, lng)
                }
            }

            body.partnerLocation?.let { loc ->
                val lat = loc.latitude
                val lng = loc.longitude
                if (lat != null && lng != null) {
                    updatePartnerMarker(lat, lng)
                }
            }

            MapShareForegroundService.start(
                context = this@MapShareActivity,
                accessToken = accessToken,
                coupleId = resolvedCoupleId,
            )
        }
    }

    private fun registerForMapUpdatesIfNeeded() {
        if (receiverRegistered) {
            return
        }

        val filter = IntentFilter().apply {
            addAction(MapShareForegroundService.ACTION_MY_LOCATION)
            addAction(MapShareForegroundService.ACTION_PARTNER_LOCATION)
        }

        ContextCompat.registerReceiver(
            this,
            mapUpdatesReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )

        receiverRegistered = true
    }

    private fun updateMyMarker(lat: Double, lng: Double) {
        val mapView = mapView ?: return
        val point = GeoPoint(lat, lng)

        val marker = myMarker ?: Marker(mapView).also {
            it.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            it.title = getString(R.string.map_share_me_marker)
            mapView.overlays.add(it)
            myMarker = it
        }

        marker.position = point
        mapView.invalidate()
    }

    private fun updatePartnerMarker(lat: Double, lng: Double) {
        val mapView = mapView ?: return
        val point = GeoPoint(lat, lng)

        val marker = partnerMarker ?: Marker(mapView).also {
            it.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            it.title = getString(R.string.map_share_partner_marker)
            mapView.overlays.add(it)
            partnerMarker = it
        }

        marker.position = point
        mapView.invalidate()
    }

    private fun centerIfNeeded(lat: Double, lng: Double) {
        if (hasCenteredOnce) {
            return
        }

        val mapView = mapView ?: return

        hasCenteredOnce = true
        mapView.controller.setZoom(DEFAULT_ZOOM)
        mapView.controller.setCenter(GeoPoint(lat, lng))
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        mapView?.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        if (receiverRegistered) {
            runCatching { unregisterReceiver(mapUpdatesReceiver) }
            receiverRegistered = false
        }

        if (!keepSharingActive) {
            MapShareForegroundService.stop(this)
        }

        mapView?.onDetach()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_ACCESS_TOKEN: String = "extra_access_token"
        const val EXTRA_COUPLE_ID: String = "extra_couple_id"

        const val EXTRA_KEEP_SHARING_ACTIVE: String = "extra_keep_sharing_active"

        private const val DEFAULT_ZOOM: Double = 17.0
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun MapShareScreen(
    onClose: () -> Unit,
    onMapReady: (MapView) -> Unit,
    onDisposeMap: () -> Unit,
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.map_share_title),
                        color = colorResource(R.color.md3_primary),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close_24),
                            contentDescription = null,
                            tint = colorResource(R.color.md3_primary),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.md3_surface),
                ),
            )
        },
        containerColor = colorResource(R.color.md3_surface_variant),
    ) { innerPadding ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            factory = {
                MapView(context).also { created ->
                    onMapReady(created)
                }
            },
        )

        DisposableEffect(Unit) {
            onDispose {
                onDisposeMap()
            }
        }
    }
}
