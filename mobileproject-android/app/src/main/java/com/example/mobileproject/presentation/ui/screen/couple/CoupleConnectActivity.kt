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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobileproject.R
import com.example.mobileproject.presentation.viewmodel.CoupleViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CoupleConnectActivity : ComponentActivity() {

    companion object {
        const val EXTRA_ACCESS_TOKEN: String = "extra_access_token"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val accessToken = intent.getStringExtra(EXTRA_ACCESS_TOKEN).orEmpty()
        enableImmersiveMode()
        setContent {
            MaterialTheme {
                val coupleViewModel: CoupleViewModel = hiltViewModel()
                CoupleConnectScreen(
                    accessToken = accessToken,
                    coupleViewModel = coupleViewModel,
                    onContinue = {
                        startActivity(
                            Intent(this, CoupleConnectedActivity::class.java)
                                .putExtra(CoupleConnectedActivity.EXTRA_ACCESS_TOKEN, accessToken)
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

    val primaryText = colorResource(R.color.auth_text_primary)
    val secondaryText = colorResource(R.color.auth_text_secondary)
    val accent = colorResource(R.color.auth_pink)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        colorResource(R.color.couple_bg_top),
                        colorResource(R.color.couple_bg_bottom)
                    )
                )
            )
            .padding(horizontal = 18.dp, vertical = 28.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("❤", style = MaterialTheme.typography.headlineMedium, color = colorResource(R.color.md3_primary))
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.couple_connect_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = primaryText
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.couple_connect_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = secondaryText
            )
            Spacer(modifier = Modifier.height(20.dp))

            Surface(shape = RoundedCornerShape(28.dp), color = colorResource(R.color.couple_card), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.couple_option_1), color = secondaryText, style = MaterialTheme.typography.labelLarge)
                    Text(stringResource(R.string.couple_generate_code), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = primaryText)
                    Text(
                        uiState.myCoupleCode ?: "--- ---",
                        style = MaterialTheme.typography.displaySmall,
                        color = colorResource(R.color.md3_primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colorResource(R.color.couple_code_bg), RoundedCornerShape(16.dp))
                            .padding(vertical = 16.dp),
                        textAlign = TextAlign.Center
                    )

                    if (!uiState.myCoupleCodeExpiresAt.isNullOrBlank()) {
                        Text(
                            text = stringResource(
                                R.string.couple_code_expires_at,
                                uiState.myCoupleCodeExpiresAt ?: "",
                            ),
                            color = secondaryText,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    if (uiState.outgoingStatus.equals("PENDING", ignoreCase = true)) {
                        Text(
                            text = stringResource(R.string.couple_waiting_partner_accept),
                            color = secondaryText,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }

                    if (uiState.outgoingStatus.equals("REJECTED", ignoreCase = true)) {
                        Text(
                            text = stringResource(R.string.couple_request_rejected),
                            color = colorResource(R.color.auth_pink),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Surface(shape = RoundedCornerShape(28.dp), color = colorResource(R.color.couple_card), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.couple_option_2), color = colorResource(R.color.md3_secondary), style = MaterialTheme.typography.labelLarge)
                    Text(stringResource(R.string.couple_enter_partner_code), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = primaryText)
                    OutlinedTextField(
                        value = partnerCode,
                        onValueChange = {
                            partnerCode = it
                            coupleViewModel.clearTransientMessages()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.couple_partner_code_hint)) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                    Button(
                        onClick = {
                            coupleViewModel.sendCoupleRequest(partnerCode)
                        },
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accent)
                    ) {
                        Text(
                            text = if (uiState.isLoading) {
                                stringResource(R.string.couple_processing)
                            } else {
                                stringResource(R.string.couple_connect_profiles)
                            },
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            if (!uiState.infoMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = uiState.infoMessage ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = secondaryText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (!uiState.errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.errorMessage ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorResource(R.color.auth_pink),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (!uiState.incomingRequestId.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = colorResource(R.color.couple_card),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.couple_incoming_request_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = primaryText,
                        )

                        Text(
                            text = uiState.incomingRequesterDisplayName
                                ?: uiState.incomingRequesterUsername
                                ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            color = secondaryText,
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    coupleViewModel.respondIncomingRequest(
                                        requestId = uiState.incomingRequestId ?: "",
                                        accept = true,
                                    )
                                },
                                enabled = !uiState.isLoading,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = accent),
                            ) {
                                Text(stringResource(R.string.couple_accept), color = Color.White)
                            }

                            Button(
                                onClick = {
                                    coupleViewModel.respondIncomingRequest(
                                        requestId = uiState.incomingRequestId ?: "",
                                        accept = false,
                                    )
                                },
                                enabled = !uiState.isLoading,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorResource(R.color.md3_secondary),
                                ),
                            ) {
                                Text(stringResource(R.string.couple_reject), color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
