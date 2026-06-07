package com.example.mobileproject.presentation.ui.screen.couple

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileproject.R
import com.example.mobileproject.presentation.ui.icons.LucideHeart
import com.example.mobileproject.presentation.ui.icons.LucideLink
import com.example.mobileproject.presentation.ui.icons.LucideShield
import com.example.mobileproject.presentation.viewmodel.CoupleViewModel



// Keeping signature compatible with CoupleConnectActivity
@Composable
fun CoupleConnectContent(
    accessToken: String,
    coupleViewModel: CoupleViewModel,
    onPairSuccess: () -> Unit,
    onSkip: () -> Unit,
) {
    var partnerCode by remember { mutableStateOf("") }
    val uiState by coupleViewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(accessToken) {
        coupleViewModel.startPolling(accessToken)
    }

    LaunchedEffect(uiState.paired) {
        if (uiState.paired) {
            onPairSuccess()
        }
    }

    // Pulse animation for the heart icon
    val pulseTransition = rememberInfiniteTransition(label = "heart-couple")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "couple-heart-scale",
    )

    val primaryPink = Color(0xFFFE8A8E)
    val lightPinkBg = Color(0xFFFFF0F1)
    val textColor = Color(0xFF5C5254)
    val grayText = Color(0xFF8C7F7B)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(lightPinkBg)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Spacer at the top
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }

            // HEART HEADER SECTION
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier
                            .size(88.dp)
                            .scale(pulseScale),
                        shape = CircleShape,
                        color = primaryPink,
                        shadowElevation = 4.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = LucideHeart,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = stringResource(R.string.couple_connect_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.couple_connect_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = grayText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(0.85f),
                        lineHeight = 20.sp
                    )
                }
            }

            // OPTION 1 CARD CONTAINER
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(32.dp)),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 28.dp)
                        ) {
                            Text(
                                text = "TÙY CHỌN 1",
                                color = primaryPink,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Tạo mã ghép đôi",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    color = textColor
                                )
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(lightPinkBg, shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.QrCode,
                                        contentDescription = null,
                                        tint = primaryPink,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Chia sẻ mã này với nửa kia của bạn để kết nối tài khoản ngay lập tức.",
                                fontSize = 14.sp,
                                color = grayText,
                                lineHeight = 20.sp
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            // Code Pill Container
                            val rawCode = uiState.myCoupleCode?.replace("-", "")?.takeIf { it.isNotBlank() } ?: "000000"
                            val formattedCode = if (rawCode.length == 6) "${rawCode.substring(0, 3)}-${rawCode.substring(3)}" else rawCode

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(lightPinkBg, shape = RoundedCornerShape(24.dp))
                                    .padding(vertical = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = formattedCode,
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryPink,
                                    letterSpacing = 2.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier
                                        .clickable(enabled = !uiState.myCoupleCode.isNullOrBlank()) {
                                            uiState.myCoupleCode?.replace("-", "")?.let {
                                                clipboardManager.setText(AnnotatedString(it))
                                            }
                                        }
                                        .padding(horizontal = 12.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ContentCopy,
                                        contentDescription = null,
                                        tint = primaryPink,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "SAO CHÉP MÃ",
                                        color = primaryPink,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            // If waiting/rejected messages inside Card 1
                            if (uiState.outgoingStatus.equals("PENDING", ignoreCase = true)) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = lightPinkBg,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "⏳ Đã gửi lời mời. Đang chờ đối tác xác nhận.",
                                        color = primaryPink,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(12.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            if (uiState.outgoingStatus.equals("REJECTED", ignoreCase = true)) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = colorScheme.errorContainer,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "❌ Đối tác đã từ chối yêu cầu. Bạn có thể gửi lại mã khác.",
                                        color = colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(12.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        // Rabbit Sticker overlaying bottom left corner INSIDE Card
                        Image(
                            painter = painterResource(R.drawable.sticker_2),
                            contentDescription = null,
                            modifier = Modifier
                                .size(95.dp)
                                .align(Alignment.BottomStart)
                                .offset(x = 8.dp, y = (-8).dp)
                        )
                    }
                }
            }

            // OPTION 2 CARD CONTAINER
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(32.dp)),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 28.dp)
                        ) {
                            Text(
                                text = "TÙY CHỌN 2",
                                color = primaryPink,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Nhập mã của nửa kia",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    color = textColor
                                )
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(lightPinkBg, shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = LucideLink,
                                        contentDescription = null,
                                        tint = primaryPink,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Nửa kia của bạn đã có mã? Nhập mã dưới đây để tham gia cùng họ.",
                                fontSize = 14.sp,
                                color = grayText,
                                lineHeight = 20.sp
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            // Centered Input Box "Ví dụ: 000-000"
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
                                        text = "Ví dụ: 000-000",
                                        color = grayText,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(50.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedContainerColor = lightPinkBg,
                                    unfocusedContainerColor = lightPinkBg,
                                    focusedTextColor = textColor,
                                    unfocusedTextColor = textColor
                                ),
                                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))

                            // Connect Profiles Button "Kết nối tài khoản ->"
                            Button(
                                onClick = {
                                    coupleViewModel.sendCoupleRequest(partnerCode)
                                },
                                enabled = !uiState.isLoading && partnerCode.isNotBlank(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = primaryPink,
                                    contentColor = Color.White,
                                    disabledContainerColor = primaryPink.copy(alpha = 0.5f)
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = if (uiState.isLoading) "Đang kết nối..." else "Kết nối tài khoản",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Rounded.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Fox Sticker overlaying bottom right corner INSIDE Card
                        Image(
                            painter = painterResource(R.drawable.sticker_12),
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp)
                                .align(Alignment.BottomEnd)
                                .offset(x = (-8).dp, y = (-8).dp)
                        )
                    }
                }
            }

            // Info Messages
            if (!uiState.infoMessage.isNullOrBlank()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "✅ ${uiState.infoMessage}",
                            color = primaryPink,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Error Messages
            if (!uiState.errorMessage.isNullOrBlank()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⚠️ ${uiState.errorMessage}",
                            color = colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Incoming Request Container (Accept / Reject)
            if (!uiState.incomingRequestId.isNullOrBlank()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(32.dp),
                        color = Color.White,
                        shadowElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Bạn có lời mời ghép đôi mới 💌",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                color = lightPinkBg
                            ) {
                                Text(
                                    text = uiState.incomingRequesterDisplayName
                                        ?: uiState.incomingRequesterUsername
                                        ?: "Một người dùng",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = primaryPink,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    textAlign = TextAlign.Center
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = {
                                        coupleViewModel.respondIncomingRequest(
                                            requestId = uiState.incomingRequestId.orEmpty(),
                                            accept = true
                                        )
                                    },
                                    enabled = !uiState.isLoading,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp),
                                    shape = RoundedCornerShape(25.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = primaryPink,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text(
                                        text = "Đồng ý",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }

                                Button(
                                    onClick = {
                                        coupleViewModel.respondIncomingRequest(
                                            requestId = uiState.incomingRequestId.orEmpty(),
                                            accept = false
                                        )
                                    },
                                    enabled = !uiState.isLoading,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp),
                                    shape = RoundedCornerShape(25.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colorScheme.surfaceContainerHigh,
                                        contentColor = textColor
                                    )
                                ) {
                                    Text(
                                        text = "Từ chối",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Security Badge and Skip for now
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Security Badge
                    Row(
                        modifier = Modifier
                            .background(primaryPink.copy(alpha = 0.1f), shape = RoundedCornerShape(50.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = LucideShield,
                            contentDescription = null,
                            tint = primaryPink,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Mã hóa bảo mật 256-bit",
                            color = primaryPink,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Skip for now
                    Text(
                        text = "Để sau, tôi sẽ thực hiện việc này sau",
                        style = MaterialTheme.typography.bodyMedium,
                        color = grayText,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable(onClick = onSkip)
                            .padding(vertical = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
