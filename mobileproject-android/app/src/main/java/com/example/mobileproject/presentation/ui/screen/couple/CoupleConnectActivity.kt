/**
 * CoupleConnectActivity - Activity host cho màn hình kết nối cặp đôi.
 *
 * Mục đích:
 * - Chứa CoupleConnectContent (màn hình kết nối với background ảnh).
 * - Khi kết nối thành công → chuyển đến [CoupleConnectedActivity].
 *
 * Chứa cả CoupleConnectScreen (phiên bản editorial layout, legacy).
 *
 * Dependency Injection:
 * - Hilt (@AndroidEntryPoint), tạo [CoupleViewModel].
 *
 * Navigation:
 * - Kết nối thành công → [CoupleConnectedActivity] (với EXTRA_ACCESS_TOKEN + EXTRA_RELATIONSHIP_START_DATE).
 *
 * Chế độ hiển thị: Immersive (ẩn status bar).
 */
package com.example.mobileproject.presentation.ui.screen.couple

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobileproject.R
import com.example.mobileproject.presentation.ui.icons.LucideHeart
import com.example.mobileproject.presentation.ui.icons.LucideLink
import com.example.mobileproject.presentation.ui.icons.LucideShare2
import com.example.mobileproject.presentation.ui.theme.MobileProjectTheme
import com.example.mobileproject.presentation.viewmodel.CoupleViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * CoupleConnectActivity - Activity host cho màn hình kết nối cặp đôi.
 *
 * Mục đích:
 * - Chứa CoupleConnectContent (màn hình kết nối với background ảnh).
 * - Khi kết nối thành công → chuyển đến [CoupleConnectedActivity].
 *
 * Dependency Injection:
 * - Hilt (@AndroidEntryPoint), tạo [CoupleViewModel].
 *
 * Navigation:
 * - Kết nối thành công → [CoupleConnectedActivity] (với EXTRA_ACCESS_TOKEN + EXTRA_RELATIONSHIP_START_DATE).
 *
 * Chế độ hiển thị: Immersive (ẩn status bar).
 */
@AndroidEntryPoint
class CoupleConnectActivity : ComponentActivity() {

    companion object {
        /** Extra key cho access token */
        const val EXTRA_ACCESS_TOKEN: String = "extra_access_token"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val accessToken = intent.getStringExtra(EXTRA_ACCESS_TOKEN).orEmpty()
        enableImmersiveMode()
        setContent {
            MobileProjectTheme {
                val coupleViewModel: CoupleViewModel = hiltViewModel()
                CoupleConnectContent(
                    accessToken = accessToken,
                    coupleViewModel = coupleViewModel,
                    onContinue = {
                        val startAt = coupleViewModel.uiState.value.startAt.orEmpty()
                        startActivity(
                            Intent(this, CoupleConnectedActivity::class.java)
                                .putExtra(CoupleConnectedActivity.EXTRA_ACCESS_TOKEN, accessToken)
                                .putExtra(CoupleConnectedActivity.EXTRA_RELATIONSHIP_START_DATE, startAt)
                        )
                        finish()
                    }
                )
            }
        }
    }

    private fun enableImmersiveMode() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}

/**
 * Composable màn hình kết nối cặp đôi (phiên bản cũ, editorial layout).
 *
 * Tương tự CoupleConnectContent nhưng sử dụng layout editorial magazine
 * với gradient code display và rounded surface cards.
 *
 * @param accessToken Token xác thực.
 * @param coupleViewModel ViewModel quản lý couple.
 * @param onContinue Callback khi kết nối thành công.
 */
@Composable
private fun CoupleConnectScreen(
    accessToken: String,
    coupleViewModel: CoupleViewModel,
    onContinue: () -> Unit,
) {
    var partnerCode by remember { mutableStateOf("") }
    val uiState by coupleViewModel.uiState.collectAsState()

    LaunchedEffect(accessToken) {
        coupleViewModel.startPolling(accessToken)
    }

    LaunchedEffect(uiState.paired) {
        if (uiState.paired) {
            onContinue()
        }
    }

    val colorScheme = MaterialTheme.colorScheme
    val surface = colorScheme.surface
    val onSurface = colorScheme.onSurface
    val surfaceContainerLow = colorScheme.surfaceContainerLow
    val primary = colorScheme.primary
    val primaryContainer = colorScheme.primaryContainer
    val primaryDim = colorScheme.primary.copy(alpha = 0.7f)
    val secondary = colorScheme.secondary
    val secondaryContainer = colorScheme.secondaryContainer
    val onSecondaryContainer = colorScheme.onSecondaryContainer
    val outlineVariant = colorScheme.outlineVariant
    val errorContainer = colorScheme.errorContainer

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(surface)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Header Section: Editorial Magazine Layout
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 40.dp)
                ) {
                    Text(
                        text = "💕",
                        style = MaterialTheme.typography.displayMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        text = stringResource(R.string.couple_connect_title),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = onSurface,
                        letterSpacing = (-0.04f).em(),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Text(
                        text = stringResource(R.string.couple_connect_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 1.6f.em(),
                        modifier = Modifier.fillMaxWidth(0.85f)
                    )
                }
            }

            // Option 1: Generate Code - Asymmetric Layout
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(48.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Header Row: Asymmetric
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.couple_option_1),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.08f.em(),
                                    color = primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.couple_generate_code),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = onSurface
                                )
                            }
                            Text("🎁", style = MaterialTheme.typography.displaySmall)
                        }

                        // Code Display: Gradient Background
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(32.dp),
                            color = Color.Transparent
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(primary, primaryContainer)
                                        ),
                                        shape = RoundedCornerShape(32.dp)
                                    )
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    uiState.myCoupleCode?.replace("-", "") ?: "------",
                                    style = MaterialTheme.typography.displaySmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.08f.em()
                                )
                            }
                        }

                        // Expiry & Status
                        if (!uiState.myCoupleCodeExpiresAt.isNullOrBlank()) {
                            Text(
                                text = "⏰ ${stringResource(
                                    R.string.couple_code_expires_at,
                                    uiState.myCoupleCodeExpiresAt ?: ""
                                )}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.08f.em()
                            )
                        }

                        if (uiState.outgoingStatus.equals("PENDING", ignoreCase = true)) {
                            Surface(
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "⏳ ${stringResource(R.string.couple_waiting_partner_accept)}",
                                    color = primary,
                                    style = MaterialTheme.typography.labelSmall,
                                    letterSpacing = 0.08f.em(),
                                    modifier = Modifier.padding(12.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        if (uiState.outgoingStatus.equals("REJECTED", ignoreCase = true)) {
                            Surface(
                                shape = RoundedCornerShape(24.dp),
                                color = errorContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "❌ ${stringResource(R.string.couple_request_rejected)}",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.labelSmall,
                                    letterSpacing = 0.08f.em(),
                                    modifier = Modifier.padding(12.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Option 2: Enter Partner Code - Asymmetric, Right-Aligned Imagery
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    shape = RoundedCornerShape(48.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Header Row: Asymmetric
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.couple_option_2),
                                    style = MaterialTheme.typography.labelMedium,
                                    letterSpacing = 0.08f.em(),
                                    color = secondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.couple_enter_partner_code),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = onSurface
                                )
                            }
                            Text("💍", style = MaterialTheme.typography.displaySmall)
                        }

                        // Input Field: Soft Well (No Borders)
                        OutlinedTextField(
                            value = partnerCode,
                            onValueChange = {
                                partnerCode = it
                                coupleViewModel.clearTransientMessages()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            placeholder = {
                                Text(
                                    stringResource(R.string.couple_partner_code_hint),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(32.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                                focusedTextColor = onSurface,
                                unfocusedTextColor = onSurface
                            ),
                            textStyle = MaterialTheme.typography.bodyLarge
                        )

                        // CTA Button: The Pill with Gradient
                        Button(
                            onClick = {
                                coupleViewModel.sendCoupleRequest(partnerCode)
                            },
                            enabled = !uiState.isLoading && partnerCode.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(32.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primary,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(primary, primaryContainer)
                                        ),
                                        shape = RoundedCornerShape(32.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (uiState.isLoading) {
                                        stringResource(R.string.couple_processing)
                                    } else {
                                        stringResource(R.string.couple_connect_profiles)
                                    },
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.labelLarge,
                                    letterSpacing = 0.08f.em()
                                )
                            }
                        }
                    }
                }
            }

            // Status Messages: No 1px Borders, Color Shifts Only
            if (!uiState.infoMessage.isNullOrBlank()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = "✅ ${uiState.infoMessage ?: ""}",
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 0.08f.em(),
                            color = primaryDim,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                }
            }

            if (!uiState.errorMessage.isNullOrBlank()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = errorContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = "⚠️ ${uiState.errorMessage ?: ""}",
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 0.08f.em(),
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                }
            }

            // Incoming Request: Soul Metric Component (Breaks Grid)
            if (!uiState.incomingRequestId.isNullOrBlank()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(48.dp),
                        color = MaterialTheme.colorScheme.primaryFixed,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp, bottom = 32.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("💌", style = MaterialTheme.typography.displaySmall)
                                Text(
                                    text = stringResource(R.string.couple_incoming_request_title),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Requester Info: Soft Well
                            Surface(
                                shape = RoundedCornerShape(32.dp),
                                color = Color.White,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = uiState.incomingRequesterDisplayName
                                        ?: uiState.incomingRequesterUsername
                                        ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = primary,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    textAlign = TextAlign.Center
                                )
                            }

                            // Action Buttons: The Pills
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = {
                                        coupleViewModel.respondIncomingRequest(
                                            requestId = uiState.incomingRequestId ?: "",
                                            accept = true,
                                        )
                                    },
                                    enabled = !uiState.isLoading,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(32.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.couple_accept),
                                        color = primary,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.08f.em()
                                    )
                                }

                                Button(
                                    onClick = {
                                        coupleViewModel.respondIncomingRequest(
                                            requestId = uiState.incomingRequestId ?: "",
                                            accept = false,
                                        )
                                    },
                                    enabled = !uiState.isLoading,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                    ),
                                    shape = RoundedCornerShape(32.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.couple_reject),
                                        color = onSurface,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.08f.em()
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Footer: Breathing Room
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// Extension for letter spacing in SP
@Composable
private fun Float.em(): androidx.compose.ui.unit.TextUnit {
    return (12 * this).sp
}
