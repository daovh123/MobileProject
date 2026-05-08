package com.example.mobileproject.presentation.ui.screen.home

import android.content.Intent
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobileproject.BuildConfig
import com.example.mobileproject.R
import com.example.mobileproject.data.datasource.remote.ApiService
import com.example.mobileproject.domain.entity.*
import com.example.mobileproject.presentation.ui.screen.couple.CoupleConnectActivity
import com.example.mobileproject.presentation.ui.screen.home.components.GoalCard
import com.example.mobileproject.presentation.viewmodel.CoupleUiState
import com.example.mobileproject.presentation.viewmodel.CoupleViewModel
import com.example.mobileproject.presentation.viewmodel.GoalViewModel
import com.example.mobileproject.presentation.viewmodel.WalletViewModel
import com.example.mobileproject.utils.formatSimpleAmount
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

@Composable
fun HomeScreen(
    accessToken: String,
    apiService: ApiService,
    onSeeAllGoals: () -> Unit,
    onNavigateToAddSavingGoal: () -> Unit,
    onNavigateToAddFutureGoal: () -> Unit
) {
    val context = LocalContext.current

    val coupleViewModel: CoupleViewModel = hiltViewModel()
    val coupleState by coupleViewModel.uiState.collectAsState()
    
    val walletViewModel: WalletViewModel = hiltViewModel()
    val walletState by walletViewModel.uiState.collectAsState()

    val goalViewModel: GoalViewModel = hiltViewModel()
    val goalState by goalViewModel.uiState.collectAsState()

    var mapView by remember { mutableStateOf<MapView?>(null) }
    var isFabExpanded by remember { mutableStateOf(false) }

    fun createMapViewIfNeeded(): MapView {
        mapView?.let { return it }
        return MapView(context).apply {
            Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(DEFAULT_ZOOM)
        }.also { mapView = it }
    }

    var myMarker by remember { mutableStateOf<Marker?>(null) }
    var hasCenteredOnce by remember { mutableStateOf(false) }

    fun centerOn(point: GeoPoint) {
        val activeMapView = mapView ?: return
        hasCenteredOnce = true
        activeMapView.controller.setZoom(DEFAULT_ZOOM)
        activeMapView.controller.setCenter(point)
    }

    LaunchedEffect(Unit) {
        walletViewModel.loadData()
        goalViewModel.loadGoals()
    }

    LaunchedEffect(coupleState.paired) {
        if (coupleState.paired) createMapViewIfNeeded()
    }

    LaunchedEffect(accessToken) {
        if (accessToken.isNotBlank()) coupleViewModel.loadStatus(accessToken)
    }

    val goals = goalState.goals
    val walletBalance = walletState.wallet?.balance ?: 0L
    val inProgressSaving = goals.filterIsInstance<SavingGoal>().filter { it.status == GoalStatus.IN_PROGRESS }.sumOf { it.currentAmount }
    val achievedSaving = goals.filterIsInstance<SavingGoal>().filter { it.status == GoalStatus.ACHIEVED }.sumOf { it.currentAmount }
    val displaySharedBalance = walletBalance + inProgressSaving - achievedSaving

    Box(modifier = Modifier.fillMaxSize()) {
        HomeContent(
            state = coupleState,
            walletBalance = displaySharedBalance,
            actualWalletBalance = walletBalance,
            daysTogether = coupleState.daysTogether?.coerceAtLeast(1) ?: computeDaysTogetherFromStartAt(coupleState.startAt) ?: 1L,
            goals = goals,
            onPairNow = {
                if (accessToken.isNotBlank()) {
                    context.startActivity(Intent(context, CoupleConnectActivity::class.java).putExtra(CoupleConnectActivity.EXTRA_ACCESS_TOKEN, accessToken))
                }
            },
            onCenterMe = { myMarker?.position?.let { centerOn(it) } },
            onSeeAllGoals = onSeeAllGoals,
            onTaskToggle = { gid, tid -> goalViewModel.toggleTask(gid, tid) },
            mapView = mapView,
            accessToken = accessToken
        )

        // FAB logic overlay
        if (isFabExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { isFabExpanded = false }
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 100.dp, end = 24.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Future Goal
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        modifier = Modifier.clickable { 
                            isFabExpanded = false
                            onNavigateToAddFutureGoal()
                        }
                    ) {
                        Text(
                            text = "Add Future Goal",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    FloatingActionButton(
                        onClick = { 
                            isFabExpanded = false
                            onNavigateToAddFutureGoal()
                        },
                        containerColor = Color.White,
                        contentColor = Color(0xFFFF8A80),
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Event, contentDescription = null)
                    }
                }

                // Saving Goal
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        modifier = Modifier.clickable { 
                            isFabExpanded = false
                            onNavigateToAddSavingGoal()
                        }
                    ) {
                        Text(
                            text = "Add Saving Goal",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    FloatingActionButton(
                        onClick = { 
                            isFabExpanded = false
                            onNavigateToAddSavingGoal()
                        },
                        containerColor = Color.White,
                        contentColor = Color(0xFFFF8A80),
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Savings, contentDescription = null)
                    }
                }
            }
        }

        val rotation by animateFloatAsState(if (isFabExpanded) 45f else 0f)
        FloatingActionButton(
            onClick = { isFabExpanded = !isFabExpanded },
            containerColor = Color(0xFFFF8A80),
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(64.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Goal", modifier = Modifier.size(32.dp).rotate(rotation))
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
    onTaskToggle: (String, String) -> Unit,
    mapView: MapView?,
    accessToken: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF0F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SharedBalanceCard(balance = walletBalance)

            DaysTogetherModernCard(daysTogether = daysTogether)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MiniMetricCard(
                    title = "SAVINGS",
                    value = formatSimpleAmount(actualWalletBalance),
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    modifier = Modifier.weight(1f)
                )
                MiniMetricCard(
                    title = "MEMORIES",
                    value = "12 New",
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    modifier = Modifier.weight(1f)
                )
            }

            // Future Goals
            val futureGoals = goals.filterIsInstance<FutureGoal>()
            GoalListSection(
                title = "Future Goals",
                goals = futureGoals,
                onSeeAllClick = onSeeAllGoals,
                onTaskToggle = onTaskToggle
            )

            // Saving Goals
            val savingGoals = goals.filterIsInstance<SavingGoal>()
            GoalListSection(
                title = "Saving Goals",
                goals = savingGoals,
                onSeeAllClick = onSeeAllGoals,
                onTaskToggle = onTaskToggle
            )

            if (!state.paired) {
                AppSurfaceCard {
                    PairSection(state = state, accessToken = accessToken, onPairNow = onPairNow)
                }
            }

            if (state.paired) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Where is your partner?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF4A3434),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    
                    Card(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        shape = RoundedCornerShape(32.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (mapView != null) {
                                AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())
                            }
                            SmallFloatingActionButton(
                                onClick = onCenterMe,
                                containerColor = Color.White,
                                contentColor = Color(0xFFFF8A80),
                                modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp),
                                shape = CircleShape
                            ) {
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
private fun GoalListSection(
    title: String,
    goals: List<Goal>,
    onSeeAllClick: () -> Unit,
    onTaskToggle: (String, String) -> Unit
) {
    if (goals.isEmpty()) return

    val sorted = goals.sortedWith(
        compareBy<Goal> { 
            when(it.status) {
                GoalStatus.IN_PROGRESS -> 0
                GoalStatus.FAILED -> 1
                GoalStatus.ACHIEVED -> 2
            }
        }.thenBy { it.deadline ?: "9999-12-31" }
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF4A3434)
            )
            TextButton(onClick = onSeeAllClick) {
                Text(text = "See All", color = Color(0xFFFF8A80), fontWeight = FontWeight.Bold)
            }
        }
        
        sorted.take(2).forEach { goal ->
            GoalCard(goal = goal, onTaskToggle = onTaskToggle)
        }
    }
}

@Composable
fun SharedBalanceCard(balance: Long) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(35.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFF8A80)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 28.dp, horizontal = 24.dp)
        ) {
            Text(
                text = "SHARED BALANCE", 
                style = MaterialTheme.typography.labelMedium, 
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatSimpleAmount(balance),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}

@Composable
fun DaysTogetherModernCard(daysTogether: Long) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(40.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = Color(0xFFFF8A80),
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "$daysTogether Days Together",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF4A3434),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Our shared journey continues",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun MiniMetricCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE4E1).copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = Color(0xFFFF8A80), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4A3434))
        }
    }
}

@Composable
private fun PairSection(state: CoupleUiState, accessToken: String, onPairNow: () -> Unit) {
    Column(modifier = Modifier.padding(20.dp)) {
        Text(text = "Connect with your partner", fontWeight = FontWeight.Bold, color = Color(0xFFFF8A80))
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onPairNow,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8A80)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Pair Now", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AppSurfaceCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        content()
    }
}

private fun computeDaysTogetherFromStartAt(startAt: String?): Long? {
    val datePart = startAt?.takeIf { it.length >= 10 }?.substring(0, 10) ?: return null
    val parts = datePart.split("-")
    if (parts.size != 3) return null
    val year = parts[0].toIntOrNull() ?: return null
    val month = parts[1].toIntOrNull() ?: return null
    val day = parts[2].toIntOrNull() ?: return null
    val utc = TimeZone.getTimeZone("UTC")
    val startCal = GregorianCalendar(utc).apply {
        set(Calendar.YEAR, year); set(Calendar.MONTH, month - 1); set(Calendar.DAY_OF_MONTH, day)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    val todayCal = GregorianCalendar(utc).apply {
        timeInMillis = System.currentTimeMillis()
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    val diffMillis = todayCal.timeInMillis - startCal.timeInMillis
    return (TimeUnit.MILLISECONDS.toDays(diffMillis) + 1).coerceAtLeast(1)
}
