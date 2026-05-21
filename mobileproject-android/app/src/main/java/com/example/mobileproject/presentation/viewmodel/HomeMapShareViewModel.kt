package com.example.mobileproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileproject.data.datasource.local.HomeMapSharingStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeMapShareViewModel @Inject constructor(
    private val homeMapSharingStore: HomeMapSharingStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeMapShareUiState())
    val uiState: StateFlow<HomeMapShareUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            homeMapSharingStore.shareLocationEnabled.collect { enabled ->
                _uiState.update { it.copy(shareLocationEnabled = enabled) }
            }
        }
    }

    fun setShareLocationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            homeMapSharingStore.setShareLocationEnabled(enabled)
        }
    }
}

data class HomeMapShareUiState(
    val shareLocationEnabled: Boolean = false,
)
