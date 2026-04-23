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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.mobileproject.presentation.ui.screen.home.HomeActivity
import com.example.mobileproject.presentation.ui.theme.MobileProjectTheme

class CoupleConnectedActivity : ComponentActivity() {

    companion object {
        const val EXTRA_ACCESS_TOKEN: String = "extra_access_token"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val accessToken = intent.getStringExtra(EXTRA_ACCESS_TOKEN).orEmpty()
        enableImmersiveMode()
        setContent {
            MobileProjectTheme {
                CoupleConnectedScreen(
                    onGoHome = {
                        startActivity(
                            Intent(this, HomeActivity::class.java)
                                .putExtra(HomeActivity.EXTRA_ACCESS_TOKEN, accessToken)
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
private fun CoupleConnectedScreen(onGoHome: () -> Unit) {
    val primaryText = colorResource(R.color.auth_text_primary)
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
            .padding(horizontal = 22.dp, vertical = 30.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🎉", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.connected_title),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = accent,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.connected_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = primaryText
            )
            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                color = colorResource(R.color.couple_card),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("❤️  🔗  ❤️", style = MaterialTheme.typography.displaySmall)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Surface(color = colorResource(R.color.couple_soft_panel), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = SeedDataProvider.relationshipStartedDate,
                    modifier = Modifier.padding(vertical = 16.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = primaryText
                )
            }
            Spacer(modifier = Modifier.height(22.dp))
            Button(
                onClick = onGoHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accent)
            ) {
                Text(stringResource(R.string.connected_go_home), color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
