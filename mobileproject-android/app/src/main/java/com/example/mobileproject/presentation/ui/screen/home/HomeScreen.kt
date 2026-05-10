package com.example.mobileproject.presentation.ui.screen.home

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobileproject.BuildConfig
import com.example.mobileproject.R
import com.example.mobileproject.data.datasource.remote.ApiService
import com.example.mobileproject.domain.entity.*
import com.example.mobileproject.presentation.service.MapShareForegroundService
import com.example.mobileproject.presentation.ui.screen.couple.CoupleConnectActivity
import com.example.mobileproject.presentation.ui.screen.home.components.GoalCard
import com.example.mobileproject.presentation.viewmodel.CoupleUiState
import com.example.mobileproject.presentation.viewmodel.CoupleViewModel
import com.example.mobileproject.presentation.viewmodel.GoalViewModel
import com.example.mobileproject.presentation.viewmodel.WalletViewModel
import com.example.mobileproject.utils.formatSimpleAmount
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit

private const val DEFAULT_ZOOM: Double = 17.0

@Composable
fun HomeScreen(
    accessToken: String,
    apiService: ApiService,
    onSeeAllGoals: () -> Unit,
    onSeeAllFutureGoals: () -> Unit,
    onNavigateToAddSavingGoal: () -> Unit,
    onNavigateToAddFutureGoal: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val coupleViewModel: CoupleViewModel = hiltViewModel()
    val coupleState by coupleViewModel.uiState.collectAsState()
    
    val walletViewModel: WalletViewModel = hiltViewModel()
    val walletState by walletViewModel.uiState.collectAsState()

    val goalViewModel: GoalViewModel = hiltViewModel()
    val goalState by goalViewModel.uiState.collectAsState()

    var mapView by remember { mutableStateOf<MapView?>(null) }
    var isFabExpanded by remember { mutableStateOf(false) }

    var myMarker by remember { mutableStateOf<Marker?>(null) }
    var partnerMarker by remember { mutableStateOf<Marker?>(null) }
    var hasCenteredOnce by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (coupleState.coupleId != null) {
                    goalViewModel.loadGoals()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Hiển thị thông báo lỗi nếu có
    LaunchedEffect(coupleState.errorMessage) {
        coupleState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    fun updateMarker(mv: MapView, isMe: Boolean, lat: Double, lng: Double) {
        val point = GeoPoint(lat, lng)
        if (isMe) {
            if (myMarker == null) {
                myMarker = Marker(mv).apply {
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = "Bạn"
                    mv.overlays.add(this)
                }
            }
            myMarker?.position = point
        } else {
            if (partnerMarker == null) {
                partnerMarker = Marker(mv).apply {
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = "Đối phương"
                    mv.overlays.add(this)
                }
            }
            partnerMarker?.position = point
        }
        mv.invalidate()
    }

    fun createMapViewIfNeeded(): MapView {
        mapView?.let { return it }
        return MapView(context).apply {
            Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(DEFAULT_ZOOM)
        }.also { mapView = it }
    }

    DisposableEffect(coupleState.paired) {
        if (!coupleState.paired) return@DisposableEffect onDispose {}

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                val mv = mapView ?: return
                val lat = intent?.getDoubleExtra(MapShareForegroundService.EXTRA_LATITUDE, Double.NaN) ?: Double.NaN
                val lng = intent?.getDoubleExtra(MapShareForegroundService.EXTRA_LONGITUDE, Double.NaN) ?: Double.NaN
                
                if (!lat.isNaN() && !lng.isNaN()) {
                    when (intent?.action) {
                        MapShareForegroundService.ACTION_MY_LOCATION -> {
                            updateMarker(mv, true, lat, lng)
                            if (!hasCenteredOnce) {
                                mv.controller.animateTo(GeoPoint(lat, lng))
                                hasCenteredOnce = true
                            }
                        }
                        MapShareForegroundService.ACTION_PARTNER_LOCATION -> {
                            updateMarker(mv, false, lat, lng)
                        }
                    }
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(MapShareForegroundService.ACTION_MY_LOCATION)
            addAction(MapShareForegroundService.ACTION_PARTNER_LOCATION)
        }
        
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        onDispose { context.unregisterReceiver(receiver) }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            coupleState.coupleId?.let { cid -> MapShareForegroundService.start(context, accessToken, cid) }
        } else {
            Toast.makeText(context, "Location permission is required to share location with your partner.", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(coupleState.paired, accessToken) {
        if (coupleState.paired && accessToken.isNotBlank()) {
            val mv = createMapViewIfNeeded()
            scope.launch {
                runCatching {
                    val resp = apiService.getMapLastLocations("Bearer $accessToken")
                    if (resp.isSuccessful && resp.body()?.success == true) {
                        resp.body()?.myLocation?.let { 
                            if (it.latitude != null && it.longitude != null) updateMarker(mv, true, it.latitude, it.longitude)
                        }
                        resp.body()?.partnerLocation?.let { 
                            if (it.latitude != null && it.longitude != null) updateMarker(mv, false, it.latitude, it.longitude)
                        }
                    }
                }
            }
            
            val hasFineLoc = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val hasCoarseLoc = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (hasFineLoc || hasCoarseLoc) {
                coupleState.coupleId?.let { cid -> MapShareForegroundService.start(context, accessToken, cid) }
            } else {
                locationPermissionLauncher.launch(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                )
            }
        }
    }

    LaunchedEffect(accessToken) {
        if (accessToken.isNotBlank()) {
            coupleViewModel.loadStatus(accessToken)
        }
    }

    LaunchedEffect(coupleState.coupleId) {
        if (coupleState.coupleId != null) {
            walletViewModel.loadData()
            goalViewModel.loadGoals()
        }
    }

    val goals = goalState.goals
    val sortedGoals = goals.sortedWith(
        compareBy<Goal> { goal ->
            val isAchieved = goal.status == GoalStatus.ACHIEVED ||
                (goal as? SavingGoal)?.let { it.targetAmount > 0 && it.currentAmount >= it.targetAmount } == true ||
                (goal as? FutureGoal)?.let { it.progress >= 100.0 } == true
            isAchieved
        }.thenBy { it.deadline ?: "9999-12-31" }
    )
    val walletBalance = walletState.wallet?.balance ?: 0L
    val inProgressSaving = sortedGoals.filterIsInstance<SavingGoal>().filter { it.status == GoalStatus.IN_PROGRESS }.sumOf { it.currentAmount }
    val achievedSaving = sortedGoals.filterIsInstance<SavingGoal>().filter { it.status == GoalStatus.ACHIEVED }.sumOf { it.currentAmount }
    val displaySharedBalance = walletBalance + inProgressSaving - achievedSaving

    Box(modifier = Modifier.fillMaxSize()) {
        HomeContent(
            state = coupleState,
            walletBalance = displaySharedBalance,
            actualWalletBalance = walletBalance,
            daysTogether = coupleState.daysTogether ?: computeDaysTogetherFromStartAt(coupleState.startAt) ?: 1L,
            goals = sortedGoals,
            onPairNow = {
                if (accessToken.isNotBlank()) {
                    context.startActivity(Intent(context, CoupleConnectActivity::class.java).putExtra(CoupleConnectActivity.EXTRA_ACCESS_TOKEN, accessToken))
                }
            },
            onCenterMe = { myMarker?.position?.let { mapView?.controller?.animateTo(it) } },
            onSeeAllGoals = onSeeAllGoals,
            onSeeAllFutureGoals = onSeeAllFutureGoals,
            onTaskToggle = { gid, tid -> goalViewModel.toggleTask(gid, tid) },
            mapView = mapView,
            accessToken = accessToken
        )

        // FAB Logic
        if (isFabExpanded) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)).clickable { isFabExpanded = false })
            Column(
                modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 100.dp, end = 24.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                GoalFabItem("Add Future Goal", Icons.Default.Event) { isFabExpanded = false; onNavigateToAddFutureGoal() }
                GoalFabItem("Add Saving Goal", Icons.Default.Savings) { isFabExpanded = false; onNavigateToAddSavingGoal() }
            }
        }

        val rotation by animateFloatAsState(if (isFabExpanded) 45f else 0f)
        FloatingActionButton(
            onClick = { isFabExpanded = !isFabExpanded },
            containerColor = Color(0xFFFF8A80),
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp).size(64.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(32.dp).rotate(rotation))
        }
    }
}

@Composable
private fun GoalFabItem(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = RoundedCornerShape(16.dp), color = Color.White, modifier = Modifier.clickable(onClick = onClick)) {
            Text(text = text, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(16.dp))
        FloatingActionButton(onClick = onClick, containerColor = Color.White, contentColor = Color(0xFFFF8A80), shape = CircleShape, modifier = Modifier.size(48.dp)) {
            Icon(imageVector = icon, contentDescription = null)
        }
    }
}

@Composable
private fun HomeContent(
    state: CoupleUiState,
    walletBalance: Long,
    actualWalletBalance: Long,
    daysTogether: Long,
    goals: List<Goal>,
    onPairNow: () -> Unit,
    onCenterMe: () -> Unit,
    onSeeAllGoals: () -> Unit,
    onSeeAllFutureGoals: () -> Unit,
    onTaskToggle: (String, String) -> Unit,
    mapView: MapView?,
    accessToken: String
) {
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFFF0F0))) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SharedBalanceCard(balance = walletBalance)
            DaysTogetherModernCard(daysTogether = daysTogether)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MiniMetricCard("SAVINGS", formatSimpleAmount(actualWalletBalance), Icons.AutoMirrored.Filled.TrendingUp, Modifier.weight(1f))
                MiniMetricCard("MEMORIES", "12 New", Icons.AutoMirrored.Filled.MenuBook, Modifier.weight(1f))
            }

            GoalListSection("Future Goals", goals.filterIsInstance<FutureGoal>(), onSeeAllFutureGoals, onTaskToggle)
            GoalListSection("Saving Goals", goals.filterIsInstance<SavingGoal>(), onSeeAllGoals, onTaskToggle)

            if (!state.paired) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    PairSection(state, accessToken, onPairNow)
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Where is your partner?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4A3434), modifier = Modifier.padding(horizontal = 8.dp))
                    Card(modifier = Modifier.fillMaxWidth().height(250.dp), shape = RoundedCornerShape(32.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (mapView != null) AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())
                            SmallFloatingActionButton(onClick = onCenterMe, containerColor = Color.White, contentColor = Color(0xFFFF8A80), modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp), shape = CircleShape) {
                                Icon(painterResource(R.drawable.ic_location_24), contentDescription = null)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun GoalListSection(title: String, goals: List<Goal>, onSeeAllClick: () -> Unit, onTaskToggle: (String, String) -> Unit) {
    if (goals.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4A3434))
            TextButton(onClick = onSeeAllClick) { Text("See All", color = Color(0xFFFF8A80), fontWeight = FontWeight.Bold) }
        }
        goals.take(2).forEach { GoalCard(it, onTaskToggle) }
    }
}

@Composable
fun SharedBalanceCard(balance: Long) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(35.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFF8A80))) {
        Column(modifier = Modifier.padding(vertical = 28.dp, horizontal = 24.dp)) {
            Text("SHARED BALANCE", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.Bold)
            Text(formatSimpleAmount(balance), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Black, color = Color.White)
        }
    }
}

@Composable
fun DaysTogetherModernCard(daysTogether: Long) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(40.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Favorite, null, tint = Color(0xFFFF8A80), modifier = Modifier.size(72.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("$daysTogether Days Together", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4A3434))
            Text("Our shared journey continues", style = MaterialTheme.typography.bodyMedium, color = Color.Gray.copy(alpha = 0.8f))
        }
    }
}

@Composable
fun MiniMetricCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(32.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE4E1).copy(alpha = 0.7f))) {
        Column(modifier = Modifier.padding(20.dp)) {
            Icon(icon, null, tint = Color(0xFFFF8A80), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4A3434))
        }
    }
}

@Composable
private fun PairSection(state: CoupleUiState, accessToken: String, onPairNow: () -> Unit) {
    Column(modifier = Modifier.padding(20.dp)) {
        Text("Connect with your partner", fontWeight = FontWeight.Bold, color = Color(0xFFFF8A80))
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onPairNow, modifier = Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8A80)), shape = RoundedCornerShape(16.dp)) {
            Text("Pair Now", fontWeight = FontWeight.Bold)
        }
    }
}

private fun computeDaysTogetherFromStartAt(startAt: String?): Long? {
    if (startAt == null || startAt.length < 10) return null
    return try {
        val parts = startAt.substring(0, 10).split("-")
        val year = parts[0].toInt()
        val month = parts[1].toInt()
        val day = parts[2].toInt()
        val utc = TimeZone.getTimeZone("UTC")
        val startCal = GregorianCalendar(utc).apply { set(year, month - 1, day, 0, 0, 0); set(Calendar.MILLISECOND, 0) }
        val todayCal = GregorianCalendar(utc).apply { timeInMillis = System.currentTimeMillis(); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
        (TimeUnit.MILLISECONDS.toDays(todayCal.timeInMillis - startCal.timeInMillis) + 1).coerceAtLeast(1)
    } catch (e: Exception) { null }
}
