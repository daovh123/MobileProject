package com.example.mobileproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.domain.repository.WalletRepository
import com.example.mobileproject.domain.usecase.wallet.GetWalletUseCase
import com.example.mobileproject.domain.usecase.wallet.UpdateWalletBalanceUseCase
import com.example.mobileproject.presentation.viewmodel.state.WalletUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val getWalletUseCase: GetWalletUseCase,
    private val updateWalletBalanceUseCase: UpdateWalletBalanceUseCase,
    private val walletRepository: WalletRepository,
    private val authSessionStore: AuthSessionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    init {
        // Observe wallet state changes from repository (Singleton)
        viewModelScope.launch {
            walletRepository.walletState.collect { wallet ->
                _uiState.update { it.copy(wallet = wallet) }
            }
        }
        
        // Auto load wallet if coupleId exists
        val session = authSessionStore.load()
        val coupleId = session?.coupleId
        if (!coupleId.isNullOrBlank()) {
            loadWallet(coupleId)
        }
    }

    fun loadWallet(coupleId: String) {
        val token = authSessionStore.load()?.token ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getWalletUseCase(coupleId, token).collect { result ->
                result.onSuccess { wallet ->
                    _uiState.update { it.copy(isLoading = false, wallet = wallet) }
                }.onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
            }
        }
    }

    fun updateBalance(newBalance: Long) {
        viewModelScope.launch {
            updateWalletBalanceUseCase(newBalance)
        }
    }
}