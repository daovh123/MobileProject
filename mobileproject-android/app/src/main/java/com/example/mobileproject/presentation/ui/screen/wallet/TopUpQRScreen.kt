package com.example.mobileproject.presentation.ui.screen.wallet

import android.graphics.Bitmap
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobileproject.presentation.model.wallet.VietnamBankCatalog
import coil.compose.AsyncImage
import com.example.mobileproject.domain.entity.TopUpRequest
import com.example.mobileproject.domain.entity.TopUpStatus
import com.example.mobileproject.presentation.ui.screen.wallet.components.BankLogo
import com.example.mobileproject.presentation.viewmodel.TopUpViewModel
import com.example.mobileproject.utils.formatSimpleAmount
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.delay

private fun generateQrBitmap(content: String, size: Int = 512): Bitmap {
    val writer = QRCodeWriter()
    val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }
    return bitmap
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopUpQRScreen(
    topUpId: String,
    onNavigateBack: () -> Unit,
    onPaymentSuccess: () -> Unit,
    viewModel: TopUpViewModel = hiltViewModel(),
) {
    val colorScheme = MaterialTheme.colorScheme
    val uiState by viewModel.uiState.collectAsState()
    val topUp = uiState.activeTopUp
    val selectedBank = remember(topUp?.bankId) { topUp?.bankId?.let(TopUpViewModel::findBankById) }

    LaunchedEffect(topUpId) {
        viewModel.startTopUpStatusPolling(topUpId)
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onPaymentSuccess()
        }
    }

    var remainingSeconds by remember(topUpId) { mutableIntStateOf(300) }
    LaunchedEffect(topUpId, topUp?.status) {
        while (remainingSeconds > 0 && topUp?.status == TopUpStatus.PENDING) {
            delay(1000L)
            remainingSeconds--
        }
    }

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val countdownText = String.format("Mã QR hết hạn sau: %02d:%02d", minutes, seconds)

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Thanh toán QR",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = colorScheme.primary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            if (topUp == null) {
                LoadingTopUpState(colorScheme = colorScheme)
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    BankLogo(bank = selectedBank, size = 42.dp)
                    Text(
                        text = topUp.bankName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                    )
                }

                TopUpStatusBadge(status = topUp.status, colorScheme = colorScheme)

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (!topUp.qrImageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = topUp.qrImageUrl,
                                contentDescription = "QR Code",
                                modifier = Modifier.size(240.dp),
                            )
                        } else if (topUp.qrContent.isNotBlank()) {
                            val qrBitmap = remember(topUp.qrContent) { generateQrBitmap(topUp.qrContent) }
                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = "QR Code",
                                modifier = Modifier.size(240.dp),
                            )
                        } else {
                            Text(
                                text = "Backend chưa trả mã QR",
                                color = colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }

                if (topUp.status == TopUpStatus.PENDING) {
                    Text(
                        text = countdownText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (remainingSeconds <= 60) colorScheme.error else colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                    )
                }

                TransferDetailsCard(topUp = topUp, colorScheme = colorScheme)

                uiState.error?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Button(
                    onClick = { viewModel.refreshTopUpStatus(topUpId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                    enabled = !uiState.isLoading && topUp.status == TopUpStatus.PENDING,
                ) {
                    if (uiState.isLoading || uiState.isPolling) {
                        CircularProgressIndicator(
                            color = colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp),
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Pending, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Tôi đã chuyển khoản, kiểm tra trạng thái",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }

                Text(
                    text = "Quét mã QR bằng app ngân hàng. Ví chỉ được cộng tiền khi backend xác nhận PAID.",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LoadingTopUpState(colorScheme: ColorScheme) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        CircularProgressIndicator(color = colorScheme.primary)
        Text(
            text = "Đang lấy thông tin chuyển khoản...",
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TopUpStatusBadge(status: TopUpStatus, colorScheme: ColorScheme) {
    val (label, tint, icon) = when (status) {
        TopUpStatus.PAID -> Triple("Đã thanh toán", Color(0xFF2E7D32), Icons.Default.CheckCircle)
        TopUpStatus.FAILED -> Triple("Thất bại", colorScheme.error, Icons.Default.ErrorOutline)
        TopUpStatus.EXPIRED -> Triple("Hết hạn", colorScheme.error, Icons.Default.ErrorOutline)
        TopUpStatus.PENDING -> Triple("Đang chờ chuyển khoản", colorScheme.primary, Icons.Default.Pending)
        TopUpStatus.UNKNOWN -> Triple("Chưa rõ trạng thái", colorScheme.onSurfaceVariant, Icons.Default.Pending)
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = tint.copy(alpha = 0.12f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            Text(
                text = label,
                color = tint,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun TransferDetailsCard(
    topUp: TopUpRequest,
    colorScheme: ColorScheme,
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TransferDetailRow("Ngân hàng", topUp.bankName, colorScheme = colorScheme)
            HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.5f))
            TransferDetailRow("Số tài khoản", topUp.accountNumber, colorScheme = colorScheme)
            HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.5f))
            TransferDetailRow("Chủ tài khoản", topUp.accountName, colorScheme = colorScheme)
            HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.5f))
            TransferDetailRow(
                label = "Số tiền",
                value = "${formatSimpleAmount(topUp.amount)} VNĐ",
                valueColor = colorScheme.primary,
                colorScheme = colorScheme,
            )
            HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.5f))
            TransferDetailRow("Mã giao dịch", topUp.transferCode, colorScheme = colorScheme)
            HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.5f))
            TransferDetailRow("Nội dung", topUp.transferContent, colorScheme = colorScheme)
        }
    }
}

@Composable
private fun TransferDetailRow(
    label: String,
    value: String,
    valueColor: Color? = null,
    colorScheme: ColorScheme,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.42f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor ?: colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.58f),
        )
    }
}
