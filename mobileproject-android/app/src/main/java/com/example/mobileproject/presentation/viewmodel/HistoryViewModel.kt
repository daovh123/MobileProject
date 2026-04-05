package com.example.mobileproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileproject.domain.entity.Place
import com.example.mobileproject.domain.repository.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val isLoading: Boolean = false,
    val history: List<Place> = emptyList(),
    val errorMessage: String? = null,
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    fun loadHistory(token: String) {
        if (token.isBlank()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    history = emptyList(),
                    errorMessage = "Phien dang nhap het han, vui long dang nhap lai",
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            historyRepository.getHistory(token)
                .onSuccess { history ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            history = history,
                            errorMessage = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Khong the tai lich su",
                        )
                    }
                }
        }
    }

    fun recordView(token: String, placeId: String) {
        if (token.isBlank() || placeId.isBlank()) {
            return
        }

        viewModelScope.launch {
            historyRepository.recordView(token, placeId)
        }
    }

    fun clearHistory(token: String) {
        if (token.isBlank()) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            historyRepository.clearHistory(token)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            history = emptyList(),
                            errorMessage = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Khong the xoa lich su",
                        )
                    }
                }
        }
    }
}
