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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import com.example.mobileproject.presentation.viewmodel.CoupleViewModel
import com.example.mobileproject.presentation.ui.icons.LucideLink
import com.example.mobileproject.presentation.ui.icons.LucideShare2
import com.example.mobileproject.presentation.ui.icons.LucideChevronRight
import com.example.mobileproject.presentation.ui.icons.LucideLock
import com.example.mobileproject.presentation.ui.theme.AppTheme

@Composable
fun CoupleConnectContent(
    accessToken: String,
    coupleViewModel: CoupleViewModel,
    onContinue: () -> Unit,
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
            onContinue()
        }
    }

    // Centered placeholder text field colors
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color(0xFFFFF0F2),
        unfocusedContainerColor = Color(0xFFFFF0F2),
        focusedBorderColor = Color.Transparent,
        unfocusedBorderColor = Color.Transparent,
        disabledBorderColor = Color.Transparent,
        focusedTextColor = Color(0xFF33181B),
        unfocusedTextColor = Color(0xFF33181B),
        cursorColor = Color(0xFFFF7E90),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF9F9),
                        Color(0xFFFFECEF),
                    )
                )
            ),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header Section: Elegant Editorial Typography
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 28.dp, bottom = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.couple_connect_title),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontSize = 32.sp,
                            lineHeight = 38.sp
                        ),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3E1C21),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(0.9f),
                    )

                    Text(
                        text = stringResource(R.string.couple_connect_subtitle),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        ),
                        color = Color(0xFF7A6064),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(0.85f),
                    )
                }
            }

            // Card 1: Generate Couple Code with 3D Overlap Judy Hopps Rabbit sticker_2
            item {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // White Base Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(32.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFF7E2E4).copy(alpha = 0.8f)),
                        shadowElevation = 6.dp,
                        tonalElevation = 2.dp,
                    ) {
                        Column(
                            modifier = Modifier.padding(top = 28.dp, bottom = 28.dp, start = 24.dp, end = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            // Header Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.couple_option_1),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontSize = 11.sp,
                                            letterSpacing = 1.5.sp
                                        ),
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFF7E90)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = stringResource(R.string.couple_generate_code),
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontSize = 22.sp,
                                            lineHeight = 26.sp
                                        ),
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2C1417)
                                    )
                                }

                                // Stylized Circular Grid Icon Button
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(Color(0xFFFFF0F2), CircleShape),
                                    contentAlignment = Alignment.Center
                               ) {
                                    // Custom beautifully aligned 2x2 grid representing QR code
                                    Column(verticalArrangement = Arrangement.spacedBy(2.5.dp)) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(2.5.dp)) {
                                            Box(modifier = Modifier.size(7.dp).background(Color(0xFFFF6D80), RoundedCornerShape(1.5.dp)))
                                            Box(modifier = Modifier.size(7.dp).background(Color(0xFFFF6D80), RoundedCornerShape(1.5.dp)))
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(2.5.dp)) {
                                            Box(modifier = Modifier.size(7.dp).background(Color(0xFFFF6D80), RoundedCornerShape(1.5.dp)))
                                            Box(modifier = Modifier.size(7.dp).background(Color(0xFFFF6D80), RoundedCornerShape(1.5.dp)))
                                        }
                                    }
                                }
                            }

                            // Subtitle Description
                            Text(
                                text = "Share this code with your partner to link your profiles instantly.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                ),
                                color = Color(0xFF8A7C7C),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Inner Rounded Pink Code Display Box
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(115.dp),
                                shape = RoundedCornerShape(24.dp),
                                color = Color(0xFFFFF0F2),
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    val rawCode = uiState.myCoupleCode.takeIf { !it.isNullOrBlank() } ?: "482910"
                                    val formattedCode = if (rawCode.length == 6) {
                                        "${rawCode.substring(0, 3)}–${rawCode.substring(3, 6)}"
                                    } else {
                                        rawCode
                                    }
                                    
                                    Text(
                                        text = formattedCode,
                                        style = MaterialTheme.typography.displaySmall.copy(
                                            fontSize = 38.sp,
                                            letterSpacing = 2.sp
                                        ),
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFF6D80),
                                    )
                                    
                                    Spacer(modifier = Modifier.height(6.dp))
                                    
                                    // Custom Copy Row
                                    Row(
                                        modifier = Modifier
                                            .clickable(enabled = !uiState.myCoupleCode.isNullOrBlank()) {
                                                uiState.myCoupleCode?.let { clipboardManager.setText(AnnotatedString(it)) }
                                            }
                                            .padding(horizontal = 12.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        // Standard Compose LucideShare2 used as a tiny beautiful placeholder copy icon
                                        Icon(
                                            imageVector = LucideShare2,
                                            contentDescription = null,
                                            tint = Color(0xFFFF7E90),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = stringResource(R.string.couple_copy_share).uppercase(),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 11.sp,
                                                letterSpacing = 1.sp
                                            ),
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFF7E90)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Judy Hopps Rabbit Overlap at Bottom-Left corner
                    Image(
                        painter = painterResource(R.drawable.sticker_2),
                        contentDescription = null,
                        modifier = Modifier
                            .size(width = 95.dp, height = 135.dp)
                            .align(Alignment.BottomStart)
                            .offset(x = (-16).dp, y = 24.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // Card 2: Enter Partner Code with 3D Overlap Nick Wilde Fox sticker_10
            item {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // White Base Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(32.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFF7E2E4).copy(alpha = 0.8f)),
                        shadowElevation = 6.dp,
                        tonalElevation = 2.dp,
                    ) {
                        Column(
                            modifier = Modifier.padding(top = 28.dp, bottom = 28.dp, start = 24.dp, end = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            // Header Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.couple_option_2),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontSize = 11.sp,
                                            letterSpacing = 1.5.sp
                                        ),
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFF7E90)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = stringResource(R.string.couple_enter_partner_code),
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontSize = 22.sp,
                                            lineHeight = 26.sp
                                        ),
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2C1417)
                                    )
                                }

                                // Stylized Circular Link Icon Button
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(Color(0xFFFFF0F2), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = LucideLink,
                                        contentDescription = null,
                                        tint = Color(0xFFFF6D80),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // Subtitle Description
                            Text(
                                text = "Does your partner already have a code? Enter it below to join them.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                ),
                                color = Color(0xFF8A7C7C),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Centered Pill-Shaped Code Text Field
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
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = stringResource(R.string.couple_partner_code_hint),
                                            color = Color(0xFFC0AFA2),
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = FontWeight.Medium,
                                                letterSpacing = 1.sp
                                            )
                                        )
                                    }
                                },
                                singleLine = true,
                                colors = fieldColors,
                                shape = CircleShape,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF33181B),
                                    letterSpacing = 2.sp
                                ),
                            )

                            // Connect Profiles CTA Button
                            Button(
                                onClick = {
                                    coupleViewModel.sendCoupleRequest(partnerCode)
                                },
                                enabled = !uiState.isLoading && partnerCode.isNotBlank(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF8E9C),
                                    contentColor = Color.White,
                                    disabledContainerColor = Color(0xFFFFD5DA)
                                ),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 2.dp,
                                    pressedElevation = 4.dp
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = stringResource(R.string.couple_connect_profiles),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontSize = 16.sp
                                        ),
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = LucideChevronRight,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Nick Wilde Fox Overlap at Bottom-Right corner (overlaps Card edge & button right edge)
                    Image(
                        painter = painterResource(R.drawable.sticker_10),
                        contentDescription = null,
                        modifier = Modifier
                            .size(width = 110.dp, height = 120.dp)
                            .align(Alignment.BottomEnd)
                            .offset(x = 16.dp, y = 20.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // Security Encryption Indicator Pill
            item {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFFEBEF),
                    border = BorderStroke(1.dp, Color(0xFFFFD5DB).copy(alpha = 0.5f)),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = LucideLock,
                            contentDescription = null,
                            tint = Color(0xFFFF7E90),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Bank-grade 256-bit encryption",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.5.sp
                            ),
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF8A6065)
                        )
                    }
                }
            }

            // Skip for Now Text Link
            item {
                Text(
                    text = stringResource(R.string.couple_do_it_later),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp
                    ),
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF7A6064),
                    modifier = Modifier
                        .clickable(onClick = onContinue)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    textAlign = TextAlign.Center,
                )
            }

            // Info messages (if any)
            if (!uiState.infoMessage.isNullOrBlank()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFF81C784).copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth(),
                        shadowElevation = 2.dp
                    ) {
                        Text(
                            text = uiState.infoMessage.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF388E3C),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            // Error messages (if any)
            if (!uiState.errorMessage.isNullOrBlank()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color(0xFFFFF0F0),
                        border = BorderStroke(1.dp, Color(0xFFFFCDD2)),
                        modifier = Modifier.fillMaxWidth(),
                        shadowElevation = 2.dp
                    ) {
                        Text(
                            text = uiState.errorMessage.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFD32F2F),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            // Preserved Pending Incoming Request Card with visual parity
            if (!uiState.incomingRequestId.isNullOrBlank()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        color = Color(0xFFFFF0F2),
                        border = BorderStroke(1.dp, Color(0xFFFFD5DB)),
                        shadowElevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("💌", fontSize = 24.sp)
                                Text(
                                    text = stringResource(R.string.couple_incoming_request_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF3E1C21),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = uiState.incomingRequesterDisplayName
                                        ?: uiState.incomingRequesterUsername
                                        ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFFF6D80),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    textAlign = TextAlign.Center
                                )
                            }

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
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFFF8E9C),
                                        contentColor = Color.White
                                    ),
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = stringResource(R.string.couple_accept),
                                        fontWeight = FontWeight.Bold,
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
                                        containerColor = Color.White,
                                        contentColor = Color(0xFF7A6064)
                                    ),
                                    border = BorderStroke(1.dp, Color(0xFFFFD5DB)),
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = stringResource(R.string.couple_reject),
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Footer Spacer
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
