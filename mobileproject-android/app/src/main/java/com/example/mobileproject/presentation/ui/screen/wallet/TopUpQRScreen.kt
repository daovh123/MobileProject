package com.example.mobileproject.presentation.ui.screen.wallet

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
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

    // Pre-populate ViewModel with navigation arguments
    LaunchedEffect(Unit) {
        viewModel.onAmountChange(amount.toString())
        viewModel.onNoteChange(note)
    }

    // Countdown timer – 5 minutes (300 seconds)
    var remainingSeconds by remember { mutableIntStateOf(300) }

    LaunchedEffect(Unit) {
        while (remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds--
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onPaymentSuccess()
        }
    }

    val qrContent = "BANK_TRANSFER|$bankId|$amount|YOUANDME_WALLET|$note"
    val qrBitmap = remember(qrContent) { generateQrBitmap(qrContent) }

    val displayNote = note.ifBlank { "Nạp tiền ví You & Me" }
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val countdownText = String.format("Mã QR hết hạn sau: %02d:%02d", minutes, seconds)

    // Deterministic color from bankId
    val bankColor = remember(bankId) {
        val hash = bankId.hashCode()
        val colors = listOf(
            Color(0xFF1976D2),
            Color(0xFF388E3C),
            Color(0xFFD32F2F),
            Color(0xFFF57C00),
            Color(0xFF7B1FA2),
            Color(0xFF00796B),
            Color(0xFFC2185B),
            Color(0xFF303F9F)
        )
        colors[Math.abs(hash) % colors.size]
    }

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Thanh toán QR",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Bank info row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = bankColor,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = bankName.firstOrNull()?.uppercase() ?: "B",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
                Text(
                    text = bankName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
            }

            // QR Code Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier.size(240.dp)
                    )
                }
            }

            // Countdown timer
            Text(
                text = countdownText,
                style = MaterialTheme.typography.bodyMedium,
                color = if (remainingSeconds <= 60) colorScheme.error else colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            // Transfer details card
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TransferDetailRow(
                        label = "Ngân hàng",
                        value = bankName,
                        colorScheme = colorScheme
                    )
                    HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.5f))
                    TransferDetailRow(
                        label = "Số tài khoản",
                        value = "1234 5678 9012",
                        colorScheme = colorScheme
                    )
                    HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.5f))
                    TransferDetailRow(
                        label = "Chủ tài khoản",
                        value = "YOU & ME WALLET",
                        colorScheme = colorScheme
                    )
                    HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.5f))
                    TransferDetailRow(
                        label = "Số tiền",
                        value = "${formatSimpleAmount(amount)} VNĐ",
                        valueColor = colorScheme.primary,
                        colorScheme = colorScheme
                    )
                    HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.5f))
                    TransferDetailRow(
                        label = "Nội dung",
                        value = displayNote,
                        colorScheme = colorScheme
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Confirm payment button
            Button(
                onClick = { viewModel.topUpNow() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Tôi đã thanh toán",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Helper text
            Text(
                text = "Quét mã QR bằng app ngân hàng để thanh toán",
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TransferDetailRow(
    label: String,
    value: String,
    valueColor: Color? = null,
    colorScheme: ColorScheme
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor ?: colorScheme.onSurface
        )
    }
}
