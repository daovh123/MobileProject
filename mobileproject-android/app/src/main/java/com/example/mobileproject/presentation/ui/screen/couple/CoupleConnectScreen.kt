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
    val extendedColors = AppTheme.extendedColors

    LaunchedEffect(accessToken) {
        coupleViewModel.startPolling(accessToken)
    }

    LaunchedEffect(uiState.paired) {
        if (uiState.paired) {
            onContinue()
        }
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = colorScheme.surfaceContainerLow.copy(alpha = 0.95f),
        unfocusedContainerColor = colorScheme.surfaceContainerLowest.copy(alpha = 0.90f),
        focusedBorderColor = colorScheme.primary,
        unfocusedBorderColor = colorScheme.outlineVariant.copy(alpha = 0.40f),
        focusedLabelColor = colorScheme.primary,
        unfocusedLabelColor = colorScheme.onSurfaceVariant,
        cursorColor = colorScheme.primary,
    )

    // Heart pulse animation
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

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        // Background image
        Image(
            painter = painterResource(R.drawable.bg_couple_connect),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.25f),
                            Color.Black.copy(alpha = 0.45f),
                            Color.Black.copy(alpha = 0.75f),
                        ),
                    ),
                ),
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Heart icon + title section
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Surface(
                        modifier = Modifier
                            .size(88.dp)
                            .scale(pulseScale),
                        shape = CircleShape,
                        color = colorScheme.primary.copy(alpha = 0.20f),
                        border = BorderStroke(2.dp, colorScheme.primary.copy(alpha = 0.35f)),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = LucideHeart,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.height(32.dp).width(32.dp),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = stringResource(R.string.couple_connect_title),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = stringResource(R.string.couple_connect_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.80f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(0.85f),
                    )
                }
            }

            // My code card (glassmorphism)
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.extraLarge,
                        color = extendedColors.glassBackground,
                        border = BorderStroke(1.dp, extendedColors.glassBorder),
                        shadowElevation = 16.dp,
                        tonalElevation = 6.dp,
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.couple_option_1),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                            )

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(96.dp),
                                shape = RoundedCornerShape(24.dp),
                                color = colorScheme.surfaceContainerLow.copy(alpha = 0.80f),
                                border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.18f)),
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize(),
                                ) {
                                    Text(
                                        text = uiState.myCoupleCode?.replace("-", "")?.takeIf { it.isNotBlank() } ?: "000000",
                                        style = MaterialTheme.typography.displaySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.primary,
                                        letterSpacing = 4.sp,
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    uiState.myCoupleCode?.replace("-", "")?.let { clipboardManager.setText(AnnotatedString(it)) }
                                },
                                enabled = !uiState.myCoupleCode.isNullOrBlank(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = MaterialTheme.shapes.extraLarge,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorScheme.primary,
                                    contentColor = colorScheme.onPrimary,
                                ),
                            ) {
                                Icon(
                                    imageVector = LucideShare2,
                                    contentDescription = null,
                                    tint = colorScheme.onPrimary,
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = stringResource(R.string.couple_copy_share),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }

                            if (uiState.outgoingStatus.equals("PENDING", ignoreCase = true)) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = colorScheme.surfaceContainerHigh.copy(alpha = 0.85f),
                                    border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.18f)),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text(
                                        text = stringResource(R.string.couple_waiting_partner_accept),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }

                            if (uiState.outgoingStatus.equals("REJECTED", ignoreCase = true)) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = colorScheme.errorContainer.copy(alpha = 0.90f),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text(
                                        text = stringResource(R.string.couple_request_rejected),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colorScheme.onErrorContainer,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }
                    }

                    Image(
                        painter = painterResource(R.drawable.sticker_2),
                        contentDescription = null,
                        modifier = Modifier
                            .size(84.dp)
                            .align(Alignment.BottomStart)
                            .offset(x = (-8).dp, y = 14.dp),
                    )
                }
            }

            // Divider
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = Color.White.copy(alpha = 0.25f),
                    )
                    Text(
                        text = stringResource(R.string.or).uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.70f),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp),
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = Color.White.copy(alpha = 0.25f),
                    )
                }
            }

            // Enter partner code card
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.extraLarge,
                        color = extendedColors.glassBackground,
                        border = BorderStroke(1.dp, extendedColors.glassBorder),
                        shadowElevation = 16.dp,
                        tonalElevation = 6.dp,
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(18.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.couple_option_2),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onSurfaceVariant,
                            )

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
                                        text = stringResource(R.string.couple_partner_code_hint),
                                        color = colorScheme.onSurfaceVariant,
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = LucideLink,
                                        contentDescription = null,
                                        tint = colorScheme.onSurfaceVariant,
                                    )
                                },
                                singleLine = true,
                                colors = fieldColors,
                                shape = MaterialTheme.shapes.extraLarge,
                                textStyle = MaterialTheme.typography.bodyLarge,
                            )

                            Button(
                                onClick = {
                                    coupleViewModel.sendCoupleRequest(partnerCode)
                                },
                                enabled = !uiState.isLoading && partnerCode.isNotBlank(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = MaterialTheme.shapes.extraLarge,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorScheme.primary,
                                    contentColor = colorScheme.onPrimary,
                                ),
                            ) {
                                Text(
                                    text = stringResource(R.string.couple_connect_now),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }

                    Image(
                        painter = painterResource(R.drawable.sticker_12),
                        contentDescription = null,
                        modifier = Modifier
                            .size(96.dp)
                            .align(Alignment.BottomEnd)
                            .offset(x = 10.dp, y = 20.dp),
                    )
                }
            }

            // Skip link
            item {
                Text(
                    text = stringResource(R.string.couple_do_it_later),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.70f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onContinue)
                        .padding(vertical = 8.dp),
                    textAlign = TextAlign.Center,
                )
            }

            // Info messages
            if (!uiState.infoMessage.isNullOrBlank()) {
                item {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.18f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                    ) {
                        Text(
                            text = uiState.infoMessage.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.primary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            if (!uiState.errorMessage.isNullOrBlank()) {
                item {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = colorScheme.errorContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                    ) {
                        Text(
                            text = uiState.errorMessage.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onErrorContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            if (!uiState.incomingRequestId.isNullOrBlank()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.extraLarge,
                        color = extendedColors.glassBackground,
                        border = BorderStroke(1.dp, extendedColors.glassBorder),
                        shadowElevation = 16.dp,
                        tonalElevation = 6.dp,
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.couple_incoming_request_title),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                            )

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                color = colorScheme.surfaceContainerLow.copy(alpha = 0.80f),
                                border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.18f)),
                            ) {
                                Text(
                                    text = uiState.incomingRequesterDisplayName
                                        ?: uiState.incomingRequesterUsername
                                        ?: "",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    textAlign = TextAlign.Center,
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Button(
                                    onClick = {
                                        coupleViewModel.respondIncomingRequest(
                                            requestId = uiState.incomingRequestId.orEmpty(),
                                            accept = true,
                                        )
                                    },
                                    enabled = !uiState.isLoading,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp),
                                    shape = MaterialTheme.shapes.extraLarge,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colorScheme.primary,
                                        contentColor = colorScheme.onPrimary,
                                    ),
                                ) {
                                    Text(
                                        text = stringResource(R.string.couple_accept),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }

                                Button(
                                    onClick = {
                                        coupleViewModel.respondIncomingRequest(
                                            requestId = uiState.incomingRequestId.orEmpty(),
                                            accept = false,
                                        )
                                    },
                                    enabled = !uiState.isLoading,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp),
                                    shape = MaterialTheme.shapes.extraLarge,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colorScheme.surfaceContainerHigh,
                                        contentColor = colorScheme.onSurface,
                                    ),
                                ) {
                                    Text(
                                        text = stringResource(R.string.couple_reject),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
