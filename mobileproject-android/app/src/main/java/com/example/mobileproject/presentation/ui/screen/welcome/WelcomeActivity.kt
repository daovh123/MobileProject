package com.example.mobileproject.presentation.ui.screen.welcome

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobileproject.R
import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.domain.entity.PostLoginDestination
import com.example.mobileproject.domain.entity.resolvePostLoginDestination
import com.example.mobileproject.presentation.ui.screen.home.HomeActivity
import com.example.mobileproject.presentation.ui.screen.login.LoginActivity
import com.example.mobileproject.presentation.ui.screen.profile.PersonalInfoActivity
import com.example.mobileproject.presentation.ui.screen.register.RegisterActivity
import com.example.mobileproject.presentation.ui.theme.MobileProjectTheme
import com.example.mobileproject.presentation.ui.theme.resolveDarkTheme
import com.example.mobileproject.presentation.viewmodel.ThemeModeViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WelcomeActivity : ComponentActivity() {

    @Inject
    lateinit var authSessionStore: AuthSessionStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableImmersiveMode()

        // Check if user is already logged in
        val savedSession = authSessionStore.load()
        if (savedSession != null) {
            val intent = when (savedSession.resolvePostLoginDestination()) {
                PostLoginDestination.PROFILE -> Intent(this, PersonalInfoActivity::class.java)
                PostLoginDestination.HOME -> Intent(this, HomeActivity::class.java)
            }.apply {
                putExtra(PersonalInfoActivity.EXTRA_ACCESS_TOKEN, savedSession.token)
                putExtra(HomeActivity.EXTRA_ACCESS_TOKEN, savedSession.token)
            }
            startActivity(intent)
            finish()
            return
        }

        setContent {
            val themeViewModel: ThemeModeViewModel = hiltViewModel()
            val themeMode by themeViewModel.themeMode.collectAsState()
            val darkTheme = themeMode.resolveDarkTheme(isSystemInDarkTheme())

            MobileProjectTheme(darkTheme = darkTheme, dynamicColor = false) {
                WelcomeScreen(
                    onGetStarted = {
                        startActivity(Intent(this, RegisterActivity::class.java))
                    },
                    onLogin = {
                        startActivity(Intent(this, LoginActivity::class.java))
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
private fun WelcomeScreen(
    onGetStarted: () -> Unit,
    onLogin: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = colorScheme.background.red < 0.5f

    // Background colors
    val bgColors = if (isDark) {
        listOf(
            Color(0xFF1A1614),
            Color(0xFF221E1C),
            Color(0xFF14110F),
        )
    } else {
        listOf(
            Color(0xFFFFF1F3),
            Color(0xFFFFE8EC),
            Color(0xFFFFF1F3),
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(colors = bgColors))
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ── Hero area: characters + hearts + ribbon with Affinity text ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .statusBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Combined stacked layout: characters on top, ribbon overlapping bottom
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        // Floating hearts decorations
                        Text(
                            text = "💕",
                            fontSize = 20.sp,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .offset(x = 20.dp, y = 4.dp)
                        )
                        Text(
                            text = "💗",
                            fontSize = 16.sp,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = (-16).dp, y = 24.dp)
                        )
                        Text(
                            text = "💖",
                            fontSize = 13.sp,
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .offset(x = 8.dp, y = (-40).dp)
                        )

                        // Sticker 16 (Fox & Rabbit characters) — centered
                        Image(
                            painter = painterResource(id = R.drawable.sticker_16),
                            contentDescription = "Affinity Characters",
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 38.dp)
                                .size(230.dp),
                            contentScale = ContentScale.Fit
                        )

                        // Sticker 17 (Pink ribbon bow) — overlapping characters bottom
                        Image(
                            painter = painterResource(id = R.drawable.sticker_17),
                            contentDescription = "Ribbon Banner",
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .offset(y = 10.dp)
                                .width(300.dp)
                                .height(80.dp),
                            contentScale = ContentScale.FillBounds
                        )

                        // Sticker 15 (Affinity script text) — ON the ribbon, big and clear
                        Image(
                            painter = painterResource(id = R.drawable.sticker_15),
                            contentDescription = "Affinity Logo",
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .offset(y = (-8).dp)
                                .width(180.dp)
                                .height(65.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }

            // ── Bottom white card panel ──
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.9f),
                shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
                color = if (isDark) colorScheme.surfaceContainer else Color.White,
                shadowElevation = 20.dp,
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp)
                        .padding(top = 28.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // ─ Title + Subtitle ─
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))

                        // Two-tone title
                        val titleLine1 = stringResource(id = R.string.welcome_title_line1)
                        val titleLine2 = stringResource(id = R.string.welcome_title_line2)

                        Text(
                            text = buildAnnotatedString {
                                withStyle(
                                    SpanStyle(
                                        color = colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold,
                                    )
                                ) {
                                    append(titleLine1)
                                }
                                append("\n")
                                withStyle(
                                    SpanStyle(
                                        color = colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                    )
                                ) {
                                    append(titleLine2)
                                }
                            },
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = 28.sp,
                                lineHeight = 38.sp
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Subtitle description
                        Text(
                            text = stringResource(id = R.string.welcome_subtitle),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                lineHeight = 22.sp
                            ),
                            color = colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp)
                        )
                    }

                    // ─ Button + Login link ─
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Large salmon/pink rounded button
                        Button(
                            onClick = onGetStarted,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDark) colorScheme.primary
                                else Color(0xFFFF8A9E),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.welcome_get_started),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // "Đã có tài khoản? Đăng nhập" link
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(id = R.string.welcome_already_have_account),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(id = R.string.welcome_login),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.primary
                                ),
                                modifier = Modifier
                                    .clickable { onLogin() }
                                    .padding(vertical = 4.dp, horizontal = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

