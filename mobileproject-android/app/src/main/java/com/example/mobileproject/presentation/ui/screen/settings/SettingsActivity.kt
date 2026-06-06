package com.example.mobileproject.presentation.ui.screen.settings

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.mobileproject.R
import com.example.mobileproject.data.datasource.local.AuthSessionStore
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import dagger.hilt.android.AndroidEntryPoint
import com.example.mobileproject.presentation.ui.theme.MobileProjectTheme
import com.example.mobileproject.presentation.ui.theme.resolveDarkTheme
import com.example.mobileproject.presentation.viewmodel.ThemeModeViewModel
import javax.inject.Inject

/**
 * Activity host cho màn hình Cài đặt – sử dụng khi Settings cần mở riêng
 * (không trong NavHost chính).
 *
 * Tính năng:
 * - Inject [AuthSessionStore] qua Hilt để lấy accessToken.
 * - Nhận accessToken từ Intent extras hoặc load từ local session.
 * - setContent với [MobileProjectTheme] (hỗ trợ dark/light theme).
 * - [SettingsActivityScreen]: Scaffold + TopAppBar + [SettingsScreen].
 *
 * @see SettingsScreen
 */
@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    @Inject
    lateinit var authSessionStore: AuthSessionStore

    companion object {
        const val EXTRA_ACCESS_TOKEN: String = "extra_access_token"
    }

    private var accessToken: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        accessToken = intent.getStringExtra(EXTRA_ACCESS_TOKEN).orEmpty()
        if (accessToken.isBlank()) {
            accessToken = authSessionStore.load()?.token.orEmpty()
        }

        setContent {
            val themeViewModel: ThemeModeViewModel = androidx.hilt.navigation.compose.hiltViewModel()
            val themeMode by themeViewModel.themeMode.collectAsState()
            val darkTheme = themeMode.resolveDarkTheme(isSystemInDarkTheme())

            MobileProjectTheme(darkTheme = darkTheme) {
                SettingsActivityScreen(
                    accessToken = accessToken,
                    onClose = { finish() },
                )
            }
        }
    }
}

/**
 * Composable host cho SettingsActivity – TopAppBar + SettingsScreen content.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SettingsActivityScreen(
    accessToken: String,
    onClose: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close_24),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            SettingsScreen(accessToken = accessToken)
        }
    }
}
