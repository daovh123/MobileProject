package com.example.mobileproject.presentation.ui.screen.wallet

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Canvas
import kotlinx.coroutines.delay

@Composable
private fun ScanCorner(
    modifier: Modifier = Modifier,
    color: Color
) {
    Canvas(modifier = modifier.size(30.dp)) {
        val strokeW = 4.dp.toPx()
        drawLine(color, Offset(0f, strokeW / 2), Offset(size.width, strokeW / 2), strokeW)
        drawLine(color, Offset(strokeW / 2, 0f), Offset(strokeW / 2, size.height), strokeW)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRScannerScreen(
    onNavigateBack: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var scanPhase by remember { mutableIntStateOf(0) }
    var isFlashOn by remember { mutableStateOf(false) }
    var showConfirmedState by remember { mutableStateOf(false) }

    // Phase 1 -> Phase 2 transition
    LaunchedEffect(scanPhase) {
        if (scanPhase == 1) {
            delay(1500L)
            scanPhase = 2
        }
    }

    // Confirmed -> navigate back
    LaunchedEffect(showConfirmedState) {
        if (showConfirmedState) {
            delay(1000L)
            onNavigateBack()
        }
    }

    val scanFrameSize = 280.dp
    val darkBackground = Color(0xFF1A1A1A)
    val successColor = Color(0xFF4CAF50)

    // Scan line animation
    val infiniteTransition = rememberInfiniteTransition(label = "scanLine")
    val scanLineProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLineProgress"
    )

    Scaffold(
        containerColor = darkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Quét mã QR",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (scanPhase) {
                0 -> {
                    // Phase 0 - Scanner UI
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Spacer(modifier = Modifier.weight(1f))

                        // Scan frame
                        Box(
                            modifier = Modifier
                                .size(scanFrameSize)
                                .clickable { scanPhase = 1 },
                            contentAlignment = Alignment.Center
                        ) {
                            // Corner brackets
                            ScanCorner(
                                modifier = Modifier.align(Alignment.TopStart),
                                color = colorScheme.primary
                            )
                            ScanCorner(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .rotate(90f),
                                color = colorScheme.primary
                            )
                            ScanCorner(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .rotate(180f),
                                color = colorScheme.primary
                            )
                            ScanCorner(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .rotate(270f),
                                color = colorScheme.primary
                            )

                            // Animated scan line
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp)
                                    .offset(
                                        y = ((scanLineProgress - 0.5f) * (scanFrameSize.value - 16f)).dp
                                    )
                                    .height(2.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                colorScheme.primary.copy(alpha = 0f),
                                                colorScheme.primary.copy(alpha = 0.8f),
                                                colorScheme.primary,
                                                colorScheme.primary.copy(alpha = 0.8f),
                                                colorScheme.primary.copy(alpha = 0f)
                                            )
                                        )
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Đưa mã QR vào khung hình",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        // Bottom controls
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 48.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Flash button
                            Surface(
                                shape = CircleShape,
                                color = colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier
                                    .size(56.dp)
                                    .clickable { isFlashOn = !isFlashOn }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isFlashOn) Icons.Filled.FlashOff else Icons.Filled.FlashOn,
                                        contentDescription = "Đèn flash",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            // Gallery button
                            Surface(
                                shape = CircleShape,
                                color = colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.PhotoLibrary,
                                        contentDescription = "Thư viện ảnh",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        TextButton(onClick = { /* Manual code input - UI only */ }) {
                            Text(
                                text = "Nhập mã thủ công",
                                color = colorScheme.primary,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }

                1 -> {
                    // Phase 1 - Scanning animation
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier.size(scanFrameSize),
                            contentAlignment = Alignment.Center
                        ) {
                            // Corner brackets in success color
                            ScanCorner(
                                modifier = Modifier.align(Alignment.TopStart),
                                color = successColor
                            )
                            ScanCorner(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .rotate(90f),
                                color = successColor
                            )
                            ScanCorner(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .rotate(180f),
                                color = successColor
                            )
                            ScanCorner(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .rotate(270f),
                                color = successColor
                            )

                            CircularProgressIndicator(
                                color = successColor,
                                modifier = Modifier.size(48.dp),
                                strokeWidth = 3.dp
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Đang xử lý...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White
                        )
                    }
                }

                2 -> {
                    // Phase 2 - Scan result success
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .widthIn(max = 320.dp)
                                .padding(24.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = colorScheme.surfaceContainerLowest
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (showConfirmedState) {
                                    // Brief success state before navigating back
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = successColor,
                                        modifier = Modifier.size(72.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Thanh toán thành công!",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = successColor
                                    )
                                } else {
                                    // Result card
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = successColor,
                                        modifier = Modifier.size(64.dp)
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = "Quét thành công!",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.onSurface
                                    )

                                    Spacer(modifier = Modifier.height(24.dp))

                                    HorizontalDivider(color = colorScheme.surfaceVariant)

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Payment info rows
                                    PaymentInfoRow(
                                        label = "Người nhận",
                                        value = "Nguyễn Văn A"
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    PaymentInfoRow(
                                        label = "Số tiền",
                                        value = "150,000 VNĐ"
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    PaymentInfoRow(
                                        label = "Nội dung",
                                        value = "Thanh toán đơn hàng"
                                    )

                                    Spacer(modifier = Modifier.height(24.dp))

                                    Button(
                                        onClick = { showConfirmedState = true },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = colorScheme.primary
                                        )
                                    ) {
                                        Text(
                                            text = "Xác nhận thanh toán",
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
        }
    }
}

@Composable
private fun PaymentInfoRow(
    label: String,
    value: String
) {
    val colorScheme = MaterialTheme.colorScheme
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
            color = colorScheme.onSurface
        )
    }
}
