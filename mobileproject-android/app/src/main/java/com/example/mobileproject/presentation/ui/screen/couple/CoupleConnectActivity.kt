package com.example.mobileproject.presentation.ui.screen.couple

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.example.mobileproject.R
import com.example.mobileproject.presentation.seed.SeedDataProvider

class CoupleConnectActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableImmersiveMode()
        setContent {
            MaterialTheme {
                CoupleConnectScreen(
                    onContinue = {
                        startActivity(Intent(this, CoupleConnectedActivity::class.java))
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
private fun CoupleConnectScreen(onContinue: () -> Unit) {
    var partnerCode by remember { mutableStateOf("") }
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
                        SeedDataProvider.generatedCoupleCode,
                        style = MaterialTheme.typography.displaySmall,
                        color = colorResource(R.color.md3_primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colorResource(R.color.couple_code_bg), RoundedCornerShape(16.dp))
                            .padding(vertical = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Surface(shape = RoundedCornerShape(28.dp), color = colorResource(R.color.couple_card), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.couple_option_2), color = colorResource(R.color.md3_secondary), style = MaterialTheme.typography.labelLarge)
                    Text(stringResource(R.string.couple_enter_partner_code), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = primaryText)
                    OutlinedTextField(
                        value = partnerCode,
                        onValueChange = { partnerCode = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.couple_partner_code_hint)) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                    Button(
                        onClick = onContinue,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accent)
                    ) {
                        Text(stringResource(R.string.couple_connect_profiles), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
