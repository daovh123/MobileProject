package com.example.mobileproject.presentation.ui.screen.home

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mobileproject.BuildConfig
import com.example.mobileproject.R
import com.example.mobileproject.data.datasource.remote.ApiService
import com.example.mobileproject.presentation.seed.SeedDataProvider
import com.example.mobileproject.presentation.service.MapShareForegroundService
import com.example.mobileproject.presentation.ui.screen.couple.CoupleConnectActivity
import com.example.mobileproject.presentation.ui.screen.map.MapShareActivity
import com.example.mobileproject.presentation.viewmodel.CoupleUiState
import com.example.mobileproject.presentation.viewmodel.CoupleViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint
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
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    @Inject
    lateinit var apiService: ApiService

    private lateinit var coupleViewModel: CoupleViewModel
    private var accessToken: String = ""

    private var mapCard: View? = null
    private var mapView: MapView? = null

    private var mapGestureDetector: GestureDetector? = null

    private var myMarker: Marker? = null
    private var partnerMarker: Marker? = null
    private var hasCenteredOnce: Boolean = false

    private var receiverRegistered: Boolean = false
    private var hasRequestedSharingPermissions: Boolean = false

    private var mapBootstrapInFlight: Boolean = false
    private var lastMapBootstrapAttemptMs: Long = 0L
    private var currentCoupleId: String? = null
    private var serviceStartedForCoupleId: String? = null

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val grantedAll = grants.values.all { it }
        if (!grantedAll) {
            Toast.makeText(requireContext(), getString(R.string.map_share_permission_denied), Toast.LENGTH_SHORT).show()
            return@registerForActivityResult
        }

        maybeStartSharingService()
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
                        if (!hasCenteredOnce) {
                            centerIfNeeded(lat, lng)
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val ARG_ACCESS_TOKEN: String = "arg_access_token"

        private const val DEFAULT_ZOOM: Double = 17.0
        private const val MAP_BOOTSTRAP_THROTTLE_MS: Long = 10_000L

        fun newInstance(accessToken: String): HomeFragment {
            return HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ACCESS_TOKEN, accessToken)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        coupleViewModel = ViewModelProvider(this)[CoupleViewModel::class.java]
        accessToken = arguments?.getString(ARG_ACCESS_TOKEN).orEmpty()

        view.findViewById<MaterialTextView>(R.id.tvHomeSeed1).text = SeedDataProvider.homeHighlights[0]
        view.findViewById<MaterialTextView>(R.id.tvHomeSeed2).text = SeedDataProvider.homeHighlights[1]
        view.findViewById<MaterialTextView>(R.id.tvHomeSeed3).text = SeedDataProvider.homeHighlights[2]

        val pairTitle = view.findViewById<MaterialTextView>(R.id.tvPairTitle)
        val pairSubtitle = view.findViewById<MaterialTextView>(R.id.tvPairSubtitle)
        val pairCode = view.findViewById<MaterialTextView>(R.id.tvPairMyCode)
        val pairHint = view.findViewById<MaterialTextView>(R.id.tvPairHint)
        val connectedCard = view.findViewById<View>(R.id.cardPairConnected)
        val partnerName = view.findViewById<MaterialTextView>(R.id.tvPairPartnerName)

        val daysTogetherCard = view.findViewById<View>(R.id.cardDaysTogether)
        val daysTogetherCount = view.findViewById<MaterialTextView>(R.id.tvDaysTogetherCount)
        val anniversaryHintCard = view.findViewById<View>(R.id.cardAnniversaryHint)

        mapCard = view.findViewById(R.id.cardPairMap)
        mapView = view.findViewById(R.id.mapHomeView)
        initMapIfNeeded()

        val centerMeButton = view.findViewById<FloatingActionButton>(R.id.fabCenterMe)
        val centerPartnerButton = view.findViewById<FloatingActionButton>(R.id.fabCenterPartner)

        centerMeButton.setOnClickListener { centerOnMarker(myMarker) }
        centerPartnerButton.setOnClickListener { centerOnMarker(partnerMarker) }

        val pairButton = view.findViewById<MaterialButton>(R.id.btnPairNow)
        pairButton.setOnClickListener {
            startActivity(
                Intent(requireContext(), CoupleConnectActivity::class.java)
                    .putExtra(CoupleConnectActivity.EXTRA_ACCESS_TOKEN, accessToken)
            )
        }

        if (accessToken.isBlank()) {
            renderNoSessionState(
                pairTitle = pairTitle,
                pairSubtitle = pairSubtitle,
                pairCode = pairCode,
                pairHint = pairHint,
                connectedCard = connectedCard,
                daysTogetherCard = daysTogetherCard,
                mapCard = mapCard,
                pairButton = pairButton,
            )
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                coupleViewModel.uiState.collect { state ->
                    renderPairState(
                        state = state,
                        pairTitle = pairTitle,
                        pairSubtitle = pairSubtitle,
                        pairCode = pairCode,
                        pairHint = pairHint,
                        connectedCard = connectedCard,
                        partnerName = partnerName,
                        daysTogetherCard = daysTogetherCard,
                        daysTogetherCount = daysTogetherCount,
                        anniversaryHintCard = anniversaryHintCard,
                        mapCard = mapCard,
                        pairButton = pairButton,
                    )
                }
            }
        }

        coupleViewModel.loadStatus(accessToken)
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
        if (accessToken.isNotBlank()) {
            coupleViewModel.loadStatus(accessToken)
        }
    }

    override fun onPause() {
        mapView?.onPause()
        super.onPause()
    }

    override fun onDestroyView() {
        if (receiverRegistered) {
            runCatching { requireContext().unregisterReceiver(mapUpdatesReceiver) }
            receiverRegistered = false
        }

        stopSharingServiceIfStarted()

        mapView?.onDetach()
        mapView = null
        mapCard = null
        mapGestureDetector = null
        myMarker = null
        partnerMarker = null
        hasCenteredOnce = false

        super.onDestroyView()
    }

    private fun renderNoSessionState(
        pairTitle: MaterialTextView,
        pairSubtitle: MaterialTextView,
        pairCode: MaterialTextView,
        pairHint: MaterialTextView,
        connectedCard: View,
        daysTogetherCard: View,
        mapCard: View?,
        pairButton: MaterialButton,
    ) {
        pairTitle.text = getString(R.string.home_pair_title)
        pairSubtitle.text = getString(R.string.home_pair_subtitle)
        pairCode.visibility = View.GONE
        connectedCard.visibility = View.GONE
        daysTogetherCard.visibility = View.GONE
        mapCard?.visibility = View.GONE
        pairHint.visibility = View.VISIBLE
        pairHint.text = getString(R.string.home_pair_session_expired)
        pairHint.setTextColor(ContextCompat.getColor(requireContext(), R.color.auth_pink))
        pairButton.visibility = View.VISIBLE
        pairButton.isEnabled = false
        pairButton.text = getString(R.string.home_pair_button)
    }

    private fun renderPairState(
        state: CoupleUiState,
        pairTitle: MaterialTextView,
        pairSubtitle: MaterialTextView,
        pairCode: MaterialTextView,
        pairHint: MaterialTextView,
        connectedCard: View,
        partnerName: MaterialTextView,
        daysTogetherCard: View,
        daysTogetherCount: MaterialTextView,
        anniversaryHintCard: View,
        mapCard: View?,
        pairButton: MaterialButton,
    ) {
        if (state.paired) {
            pairTitle.text = getString(R.string.home_pair_connected_title)
            pairSubtitle.text = getString(R.string.home_pair_connected_subtitle)
            pairCode.visibility = View.GONE
            pairHint.visibility = View.GONE
            pairButton.visibility = View.GONE
            connectedCard.visibility = View.VISIBLE

            daysTogetherCard.visibility = View.VISIBLE
            val computedDaysTogether = state.daysTogether?.coerceAtLeast(1)
                ?: computeDaysTogetherFromStartAt(state.startAt)
                ?: 1L
            daysTogetherCount.text = computedDaysTogether.toString()

            mapCard?.visibility = View.VISIBLE
            bootstrapMapIfNeeded()

            val showHappyAnniversary = state.anniversaryTomorrow == true
            anniversaryHintCard.visibility = if (showHappyAnniversary) View.VISIBLE else View.GONE

            val partnerDisplayName = state.partnerUsername ?: getString(R.string.home_pair_partner_unknown)
            partnerName.text = getString(R.string.home_pair_partner_name, partnerDisplayName)
            return
        }

        pairTitle.text = getString(R.string.home_pair_title)
        pairSubtitle.text = getString(R.string.home_pair_subtitle)
        connectedCard.visibility = View.GONE
        daysTogetherCard.visibility = View.GONE
        mapCard?.visibility = View.GONE
        stopSharingServiceIfStarted()
        pairButton.visibility = View.VISIBLE
        pairButton.isEnabled = !state.isLoading
        pairButton.text = if (state.isLoading) {
            getString(R.string.home_pair_loading_button)
        } else {
            getString(R.string.home_pair_button)
        }

        val myCode = state.myCoupleCode
        if (myCode.isNullOrBlank()) {
            pairCode.visibility = View.GONE
        } else {
            pairCode.visibility = View.VISIBLE
            pairCode.text = getString(R.string.home_pair_my_code, myCode)
        }

        val hintText = when {
            !state.errorMessage.isNullOrBlank() -> state.errorMessage
            state.outgoingStatus.equals("PENDING", ignoreCase = true) -> getString(R.string.home_pair_outgoing_pending)
            state.outgoingStatus.equals("REJECTED", ignoreCase = true) -> getString(R.string.home_pair_outgoing_rejected)
            !state.myCoupleCodeExpiresAt.isNullOrBlank() -> getString(
                R.string.home_pair_code_expiry,
                state.myCoupleCodeExpiresAt ?: "",
            )
            else -> null
        }

        if (hintText.isNullOrBlank()) {
            pairHint.visibility = View.GONE
            return
        }

        val hintColor = if (
            !state.errorMessage.isNullOrBlank() ||
            state.outgoingStatus.equals("REJECTED", ignoreCase = true)
        ) {
            R.color.auth_pink
        } else {
            R.color.md3_on_surface_variant
        }

        pairHint.visibility = View.VISIBLE
        pairHint.text = hintText
        pairHint.setTextColor(ContextCompat.getColor(requireContext(), hintColor))
    }

    private fun initMapIfNeeded() {
        val view = mapView ?: return

        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID

        view.setTileSource(TileSourceFactory.MAPNIK)
        view.setMultiTouchControls(true)
        view.controller.setZoom(DEFAULT_ZOOM)

        if (mapGestureDetector == null) {
            mapGestureDetector = GestureDetector(
                requireContext(),
                object : GestureDetector.SimpleOnGestureListener() {
                    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                        openFullScreenMap()
                        return true
                    }
                }
            )

            view.setOnTouchListener { _, event ->
                mapGestureDetector?.onTouchEvent(event)
                false
            }
        }
    }

    private fun openFullScreenMap() {
        if (accessToken.isBlank()) {
            return
        }

        val intent = Intent(requireContext(), MapShareActivity::class.java)
            .putExtra(MapShareActivity.EXTRA_ACCESS_TOKEN, accessToken)
            .putExtra(MapShareActivity.EXTRA_KEEP_SHARING_ACTIVE, true)

        val coupleId = currentCoupleId?.trim().orEmpty()
        if (coupleId.isNotBlank()) {
            intent.putExtra(MapShareActivity.EXTRA_COUPLE_ID, coupleId)
        }

        startActivity(intent)
    }

    private fun centerOnMarker(marker: Marker?) {
        val view = mapView ?: return
        val point = marker?.position ?: return

        hasCenteredOnce = true
        view.controller.setZoom(DEFAULT_ZOOM)
        view.controller.setCenter(point)
    }

    private fun bootstrapMapIfNeeded() {
        if (accessToken.isBlank()) {
            return
        }

        val now = System.currentTimeMillis()
        if (mapBootstrapInFlight) {
            return
        }
        if (now - lastMapBootstrapAttemptMs < MAP_BOOTSTRAP_THROTTLE_MS) {
            return
        }

        lastMapBootstrapAttemptMs = now
        mapBootstrapInFlight = true

        registerForMapUpdatesIfNeeded()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val tokenHeader = "Bearer ${accessToken.trim()}"

                val response = runCatching { apiService.getMapLastLocations(tokenHeader) }.getOrNull()
                val body = response?.body()

                if (response == null || !response.isSuccessful || body == null || !body.success) {
                    return@launch
                }

                val resolvedCoupleId = body.coupleId?.trim().orEmpty()
                if (resolvedCoupleId.isNotBlank()) {
                    currentCoupleId = resolvedCoupleId
                }

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
            requireContext(),
            mapUpdatesReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )

        receiverRegistered = true
    }

    private fun maybeStartSharingService() {
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
                permissionLauncher.launch(missing.toTypedArray())
            }
            return
        }

        MapShareForegroundService.start(requireContext(), accessToken, coupleId)
        serviceStartedForCoupleId = coupleId
    }

    private fun stopSharingServiceIfStarted() {
        if (serviceStartedForCoupleId == null) {
            return
        }

        MapShareForegroundService.stop(requireContext())
        serviceStartedForCoupleId = null
    }

    private fun missingSharingPermissions(): List<String> {
        val required = buildList {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        return required.filter { permission ->
            ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED
        }
    }

    private fun updateMyMarker(lat: Double, lng: Double) {
        val view = mapView ?: return
        val point = GeoPoint(lat, lng)

        val marker = myMarker ?: Marker(view).also {
            it.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            it.title = getString(R.string.map_share_me_marker)
            view.overlays.add(it)
            myMarker = it
        }

        marker.position = point
        view.invalidate()
    }

    private fun updatePartnerMarker(lat: Double, lng: Double) {
        val view = mapView ?: return
        val point = GeoPoint(lat, lng)

        val marker = partnerMarker ?: Marker(view).also {
            it.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            it.title = getString(R.string.map_share_partner_marker)
            view.overlays.add(it)
            partnerMarker = it
        }

        marker.position = point
        view.invalidate()
    }

    private fun centerIfNeeded(lat: Double, lng: Double) {
        val view = mapView ?: return
        if (hasCenteredOnce) {
            return
        }

        hasCenteredOnce = true
        view.controller.setZoom(DEFAULT_ZOOM)
        view.controller.setCenter(GeoPoint(lat, lng))
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
}
