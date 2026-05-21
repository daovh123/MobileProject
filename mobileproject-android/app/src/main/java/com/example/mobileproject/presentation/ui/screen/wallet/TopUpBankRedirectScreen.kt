package com.example.mobileproject.presentation.ui.screen.wallet

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobileproject.presentation.viewmodel.TopUpViewModel
import com.example.mobileproject.utils.formatSimpleAmount
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopUpBankRedirectScreen(
    amount: Long,
    bankId: String,
    bankName: String,
    note: String,
    onNavigateBack: () -> Unit,
    onPaymentSuccess: () -> Unit,
    viewModel: TopUpViewModel = hiltViewModel()
) {
    val colorScheme = MaterialTheme.colorScheme
    val uiState by viewModel.uiState.collectAsState()

    var phase by remember { mutableIntStateOf(0) }

    // Pre-populate ViewModel with navigation arguments
    LaunchedEffect(Unit) {
        viewModel.onAmountChange(amount.toString())
        viewModel.onNoteChange(note)
    }

    // Phase 0 → Phase 1: wait 2 seconds then advance
    LaunchedEffect(phase) {
        if (phase == 0) {
            delay(2000)
            phase = 1
        }
    }

    // Phase 1 → Phase 2: trigger top-up and wait 1.5 seconds
    LaunchedEffect(phase) {
        if (phase == 1) {
            viewModel.topUpNow()
            delay(1500)
            phase = 2
        }
    }

    // Observe success state
    LaunchedEffect(uiState.isSuccess) {
        // No-op: success is visually handled by phase 2
    }

    // Scale animation for the checkmark in phase 2
    val checkmarkScale by animateFloatAsState(
        targetValue = if (phase == 2) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "checkmarkScale"
    )

    Scaffold(
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Back button – visible only in phase 0 and 1
            if (phase < 2) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = colorScheme.primary
                    )
                }
            }

            // Center content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when (phase) {
                    0 -> {
                        // Phase 0 – Redirecting
                        CircularProgressIndicator(
                            modifier = Modifier.size(64.dp),
                            color = colorScheme.primary,
                            strokeWidth = 5.dp
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            text = "Đang chuyển tiếp tới",
                            style = MaterialTheme.typography.bodyLarge,
                            color = colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = bankName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Vui lòng không tắt ứng dụng...",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }

                    1 -> {
                        // Phase 1 – Processing
                        CircularProgressIndicator(
                            modifier = Modifier.size(64.dp),
                            color = colorScheme.primary,
                            strokeWidth = 5.dp
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            text = "Đang xử lý thanh toán...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = formatSimpleAmount(amount),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Vui lòng không tắt ứng dụng...",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }

                    2 -> {
                        // Phase 2 – Success
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Thành công",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier
                                .size(80.dp)
                                .scale(checkmarkScale)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Nạp tiền thành công!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = formatSimpleAmount(amount),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = bankName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(40.dp))
                        Button(
                            onClick = onPaymentSuccess,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            shape = RoundedCornerShape(32.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Text(
                                text = "Quay lại ví",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
