package com.example.mobileproject.presentation.ui.screen.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mobileproject.R
import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.presentation.ui.screen.login.LoginActivity
import com.example.mobileproject.presentation.viewmodel.AuthViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    @Inject
    lateinit var authSessionStore: AuthSessionStore

    companion object {
        const val EXTRA_ACCESS_TOKEN: String = "extra_access_token"
    }

    private lateinit var authViewModel: AuthViewModel
    private var accessToken: String = ""
    private var logoutRequested: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        accessToken = intent.getStringExtra(EXTRA_ACCESS_TOKEN).orEmpty()
        if (accessToken.isBlank()) {
            accessToken = authSessionStore.load()?.token.orEmpty()
        }
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        val topAppBar = findViewById<MaterialToolbar>(R.id.topAppBarSettings)
        val logoutButton = findViewById<MaterialButton>(R.id.btnLogout)

        topAppBar.setNavigationOnClickListener {
            finish()
        }

        logoutButton.setOnClickListener {
            performLogout()
        }

        observeLogoutState(logoutButton)
    }

    private fun observeLogoutState(logoutButton: MaterialButton) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.uiState.collect { state ->
                    logoutButton.isEnabled = !state.isLoading

                    if (state.logoutCompleted) {
                        authViewModel.consumeLogoutSuccess()
                        logoutRequested = false
                        Toast.makeText(this@SettingsActivity, getString(R.string.logout_success), Toast.LENGTH_SHORT).show()
                        navigateToLogin()
                    }

                    if (logoutRequested && !state.errorMessage.isNullOrBlank()) {
                        logoutRequested = false
                        Toast.makeText(this@SettingsActivity, state.errorMessage, Toast.LENGTH_SHORT).show()
                        authViewModel.clearError()
                    }
                }
            }
        }
    }

    private fun performLogout() {
        if (logoutRequested) {
            return
        }

        if (accessToken.isBlank()) {
            navigateToLogin()
            return
        }

        logoutRequested = true
        authViewModel.logout(accessToken)
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intent)
        finish()
    }
}
