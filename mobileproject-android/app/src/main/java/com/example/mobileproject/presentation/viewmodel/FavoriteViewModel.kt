package com.example.mobileproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileproject.domain.entity.Place
import com.example.mobileproject.domain.repository.FavoriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoriteUiState(
    val isLoading: Boolean = false,
    val favorites: List<Place> = emptyList(),
    val errorMessage: String? = null,
    val favoriteIds: Set<String> = emptySet(),
    val toggleInProgress: Set<String> = emptySet(),
)

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
) : ViewModel() {

    private companion object {
        const val FAVORITES_STALE_MS: Long = 60_000L
    }

    private val _uiState = MutableStateFlow(FavoriteUiState())
    val uiState: StateFlow<FavoriteUiState> = _uiState.asStateFlow()
    private var lastLoadedToken: String? = null
    private var lastLoadedAt: Long = 0L

    fun ensureLoaded(token: String, force: Boolean = false) {
        val trimmedToken = token.trim()
        if (trimmedToken.isBlank()) {
            loadFavorites(token)
            return
        }
        val hasLoadedForToken = trimmedToken == lastLoadedToken && lastLoadedAt > 0L
        val isFresh = System.currentTimeMillis() - lastLoadedAt < FAVORITES_STALE_MS
        if (!force && hasLoadedForToken && isFresh) {
            return
        }
        loadFavorites(trimmedToken, showLoading = !hasLoadedForToken)
    }

    fun refreshIfStale(token: String) {
        ensureLoaded(token, force = false)
    }

    fun loadFavorites(token: String, showLoading: Boolean = true) {
        if (token.isBlank()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    favorites = emptyList(),
                    favoriteIds = emptySet(),
                    errorMessage = "Phien dang nhap het han, vui long dang nhap lai",
                )
            }
            return
        }

        viewModelScope.launch {
            if (showLoading) {
                _uiState.update { state ->
                    state.copy(isLoading = state.favoriteIds.isEmpty(), errorMessage = null)
                }
            } else {
                _uiState.update { it.copy(errorMessage = null) }
            }

            favoriteRepository.getFavorites(token)
                .onSuccess { favorites ->
                    lastLoadedToken = token.trim()
                    lastLoadedAt = System.currentTimeMillis()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            favorites = favorites,
                            favoriteIds = favorites.map { place -> place.id }.toSet(),
                            errorMessage = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Khong the tai danh sach yeu thich",
                        )
                    }
                }
        }
    }

    fun toggleFavorite(token: String, placeId: String) {
        if (token.isBlank() || placeId.isBlank()) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(toggleInProgress = it.toggleInProgress + placeId) }

            favoriteRepository.toggleFavorite(token, placeId)
                .onSuccess { isNowFavorite ->
                    _uiState.update { current ->
                        val newIds = if (isNowFavorite) {
                            current.favoriteIds + placeId
                        } else {
                            current.favoriteIds - placeId
                        }

                        val newFavorites = when {
                            isNowFavorite -> current.favorites
                            else -> current.favorites.filterNot { it.id == placeId }
                        }

                        current.copy(
                            favoriteIds = newIds,
                            favorites = newFavorites,
                            toggleInProgress = current.toggleInProgress - placeId,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update { current ->
                        current.copy(
                            toggleInProgress = current.toggleInProgress - placeId,
                            errorMessage = throwable.message ?: "Khong the cap nhat yeu thich",
                        )
                    }
                }
        }
    }

    fun checkFavorite(token: String, placeId: String) {
        if (token.isBlank() || placeId.isBlank()) {
            return
        }

        viewModelScope.launch {
            favoriteRepository.checkFavorite(token, placeId)
                .onSuccess { isFavorite ->
                    _uiState.update { current ->
                        val newIds = if (isFavorite) {
                            current.favoriteIds + placeId
                        } else {
                            current.favoriteIds - placeId
                        }
                        current.copy(favoriteIds = newIds)
                    }
                }
        }
    }
}
