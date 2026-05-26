package com.example.mobileproject.presentation.ui.screen.wallet

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobileproject.presentation.model.wallet.VietnamBankCatalog
import com.example.mobileproject.domain.entity.TopUpStatus
import com.example.mobileproject.presentation.ui.screen.wallet.components.BankLogo
import com.example.mobileproject.presentation.viewmodel.TopUpViewModel
import com.example.mobileproject.utils.formatSimpleAmount
import kotlinx.coroutines.delay

@Composable
fun TopUpBankRedirectScreen(
    topUpId: String,
    onNavigateBack: () -> Unit,
    onPaymentSuccess: () -> Unit,
    viewModel: TopUpViewModel = hiltViewModel(),
) {
    val colorScheme = MaterialTheme.colorScheme
    val uiState by viewModel.uiState.collectAsState()
    val topUp = uiState.activeTopUp
    val selectedBank = remember(topUp?.bankId) { topUp?.bankId?.let(TopUpViewModel::findBankById) }

    var phase by remember(topUpId) { mutableIntStateOf(0) }

    LaunchedEffect(topUpId) {
        delay(1200)
        phase = 1
        viewModel.startTopUpStatusPolling(topUpId)
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            phase = 2
        }
    }

    val checkmarkScale by animateFloatAsState(
        targetValue = if (phase == 2) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "checkmarkScale",
    )

    Scaffold(containerColor = Color.Transparent) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp),
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = colorScheme.primary,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                when {
                    phase == 0 || topUp == null -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(64.dp),
                            color = colorScheme.primary,
                            strokeWidth = 5.dp,
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Đang chuẩn bị yêu cầu chuyển khoản...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        )
                    }

                    topUp.status == TopUpStatus.PAID -> {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Thành công",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier
                                .size(80.dp)
                                .scale(checkmarkScale),
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Nạp tiền thành công!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = formatSimpleAmount(topUp.amount),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        BankLogo(bank = selectedBank, size = 42.dp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = topUp.bankName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(36.dp))
                        Button(
                            onClick = onPaymentSuccess,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            shape = RoundedCornerShape(32.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                        ) {
                            Text(
                                text = "Quay lại ví",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                    else -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(64.dp),
                            color = colorScheme.primary,
                            strokeWidth = 5.dp,
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        BankLogo(bank = selectedBank, size = 56.dp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Đang chờ ngân hàng xác nhận",
                            style = MaterialTheme.typography.bodyLarge,
                            color = colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = formatSimpleAmount(topUp.amount),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        TransferSummary(topUp = topUp)
                        Spacer(modifier = Modifier.height(24.dp))
                        uiState.error?.let { message ->
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        Button(
                            onClick = { viewModel.refreshTopUpStatus(topUpId) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            shape = RoundedCornerShape(30.dp),
                            enabled = !uiState.isLoading && topUp.status == TopUpStatus.PENDING,
                        ) {
                            if (uiState.isLoading || uiState.isPolling) {
                                CircularProgressIndicator(
                                    color = colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp),
                                )
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Pending, contentDescription = null, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Kiểm tra trạng thái",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Hãy chuyển đúng nội dung để Sepay/webhook đối soát và backend cập nhật PAID.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransferSummary(topUp: com.example.mobileproject.domain.entity.TopUpRequest) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colorScheme.surfaceVariant,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = topUp.bankName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "${topUp.accountNumber} - ${topUp.accountName}",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = topUp.transferContent,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
