package com.example.mobileproject.presentation.ui.screen.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mobileproject.R
import com.example.mobileproject.presentation.ui.screen.login.LoginActivity
import com.example.mobileproject.presentation.viewmodel.AuthViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    companion object {
        private const val ARG_ACCESS_TOKEN: String = "arg_access_token"

        fun newInstance(accessToken: String): SettingsFragment {
            return SettingsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ACCESS_TOKEN, accessToken)
                }
            }
        }
    }

    private lateinit var authViewModel: AuthViewModel
    private var accessToken: String = ""
    private var logoutRequested: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        accessToken = arguments?.getString(ARG_ACCESS_TOKEN).orEmpty()

        val logoutButton = view.findViewById<MaterialButton>(R.id.btnSettingsLogout)
        val logoutStatus = view.findViewById<MaterialTextView>(R.id.tvSettingsLogoutStatus)

        logoutButton.setOnClickListener {
            performLogout()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.uiState.collect { state ->
                    logoutButton.isEnabled = !state.isLoading
                    if (state.isLoading && logoutRequested) {
                        logoutStatus.visibility = View.VISIBLE
                        logoutStatus.text = getString(R.string.settings_logout_processing)
                        logoutStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.md3_on_surface_variant))
                    }

                    if (state.logoutCompleted) {
                        authViewModel.consumeLogoutSuccess()
                        logoutRequested = false
                        Toast.makeText(requireContext(), getString(R.string.logout_success), Toast.LENGTH_SHORT).show()
                        navigateToLogin()
                    }

                    if (logoutRequested && !state.errorMessage.isNullOrBlank()) {
                        logoutRequested = false
                        logoutStatus.visibility = View.VISIBLE
                        logoutStatus.text = state.errorMessage
                        logoutStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.auth_pink))
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
        val intent = Intent(requireContext(), LoginActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intent)
        requireActivity().finish()
    }
}
