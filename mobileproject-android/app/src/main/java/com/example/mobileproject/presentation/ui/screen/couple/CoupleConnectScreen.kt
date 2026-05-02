package com.example.mobileproject.presentation.ui.screen.couple

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
import com.example.mobileproject.R
import com.example.mobileproject.presentation.ui.icons.LucideHeart
import com.example.mobileproject.presentation.viewmodel.CoupleViewModel
import com.example.mobileproject.presentation.ui.icons.LucideLink
import com.example.mobileproject.presentation.ui.icons.LucideShare2

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

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = colorScheme.surfaceVariant.copy(alpha = 0.92f),
        unfocusedContainerColor = colorScheme.surface.copy(alpha = 0.95f),
        focusedBorderColor = colorScheme.primary,
        unfocusedBorderColor = colorScheme.outline.copy(alpha = 0.28f),
        focusedLabelColor = colorScheme.primary,
        unfocusedLabelColor = colorScheme.onSurfaceVariant,
        cursorColor = colorScheme.primary,
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Surface(
                        modifier = Modifier.size(88.dp),
                        shape = CircleShape,
                        color = colorScheme.primary.copy(alpha = 0.16f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = LucideHeart,
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.height(32.dp).width(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = stringResource(R.string.couple_connect_title),
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = stringResource(R.string.couple_connect_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(0.85f),
                    )
                }
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    shadowElevation = 10.dp,
                    tonalElevation = 6.dp,
                    color = colorScheme.surface,
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
                            color = colorScheme.surfaceVariant.copy(alpha = 0.68f),
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = uiState.myCoupleCode.takeIf { !it.isNullOrBlank() } ?: "000000",
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.primary,
                                    letterSpacing = 2.sp,
                                )
                            }
                        }

                        Button(
                            onClick = {
                                uiState.myCoupleCode?.let { clipboardManager.setText(AnnotatedString(it)) }
                            },
                            enabled = !uiState.myCoupleCode.isNullOrBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(28.dp),
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
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Divider(
                        modifier = Modifier.weight(1f),
                        color = colorScheme.onSurface.copy(alpha = 0.16f),
                    )
                    Text(
                        text = stringResource(R.string.or).uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 14.dp),
                    )
                    Divider(
                        modifier = Modifier.weight(1f),
                        color = colorScheme.onSurface.copy(alpha = 0.16f),
                    )
                }
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    shadowElevation = 10.dp,
                    tonalElevation = 6.dp,
                    color = colorScheme.surface,
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
                            shape = RoundedCornerShape(28.dp),
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
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorScheme.primary,
                                contentColor = colorScheme.onPrimary,
                            ),
                        ) {
                            Text(
                                text = stringResource(R.string.couple_connect_now),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = stringResource(R.string.couple_do_it_later),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onContinue)
                        .padding(vertical = 8.dp),
                    textAlign = TextAlign.Center,
                )
            }

            if (!uiState.infoMessage.isNullOrBlank()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = colorScheme.surfaceVariant,
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
                        shape = RoundedCornerShape(24.dp),
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

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
