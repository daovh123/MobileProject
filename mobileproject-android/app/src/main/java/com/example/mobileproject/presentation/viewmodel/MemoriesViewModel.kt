package com.example.mobileproject.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.data.datasource.remote.ApiService
import com.example.mobileproject.data.model.moment.MomentCommentDto
import com.example.mobileproject.data.model.moment.MomentCommentRequestDto
import com.example.mobileproject.data.model.moment.MomentDto
import com.example.mobileproject.data.model.moment.MomentReactionRequestDto
import com.example.mobileproject.data.model.moment.MomentReactionResponseDto
import com.example.mobileproject.data.model.moment.MomentRequestDto
import com.example.mobileproject.domain.repository.OnboardingRepository
import com.example.mobileproject.presentation.widget.MomentWidgetUpdater
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemoriesViewModel @Inject constructor(
    private val apiService: ApiService,
    private val onboardingRepository: OnboardingRepository,
    private val authSessionStore: AuthSessionStore,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MemoriesUiState())
    val uiState: StateFlow<MemoriesUiState> = _uiState.asStateFlow()

    private var accessToken: String? = null

    fun load(accessToken: String) {
        if (accessToken.isBlank()) return
        this.accessToken = accessToken.trim()
        val session = authSessionStore.load()
        _uiState.update { it.copy(currentUsername = session?.username.orEmpty()) }
        refreshMoments()
    }

    fun refreshMoments(showLoading: Boolean = true) {
        val token = accessToken ?: return
        viewModelScope.launch {
            if (showLoading) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            } else {
                _uiState.update { it.copy(errorMessage = null) }
            }
            val coupleId = resolveCoupleId(token)
            if (coupleId.isNullOrBlank()) {
                _uiState.update { it.copy(isLoading = false, moments = emptyList(), errorMessage = "Ban chua ghep doi") }
                updateWidgetLatest(null)
                return@launch
            }

            val authHeader = "Bearer $token"
            runCatching {
                apiService.getMoments(coupleId, authHeader)
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    val moments = response.body().orEmpty()
                    _uiState.update { it.copy(isLoading = false, moments = moments, coupleId = coupleId) }
                    updateWidgetLatest(moments.firstOrNull())
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Khong the tai khoanh khac") }
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Khong the tai khoanh khac"
                    )
                }
            }
        }
    }

    suspend fun saveMoment(title: String, base64Image: String): Boolean {
        val token = accessToken ?: return false
        val coupleId = resolveCoupleId(token)
        if (coupleId.isNullOrBlank()) {
            _uiState.update { it.copy(errorMessage = "Ban chua ghep doi") }
            return false
        }

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        val authHeader = "Bearer $token"
        val response = runCatching {
            val request = MomentRequestDto(coupleId, title, base64Image)
            apiService.createMoment(request, authHeader)
        }.getOrElse { throwable ->
            _uiState.update { it.copy(isSaving = false, errorMessage = throwable.message ?: "Luu khoanh khac that bai") }
            return false
        }

        if (response.isSuccessful && response.body()?.success == true) {
            val created = response.body()?.moment
            if (created != null) {
                _uiState.update { state ->
                    val updated = listOf(created) + state.moments.filterNot { it.id == created.id }
                    state.copy(isSaving = false, moments = updated, coupleId = coupleId)
                }
                updateWidgetLatest(created)
            } else {
                _uiState.update { it.copy(isSaving = false) }
            }
            refreshMoments(showLoading = false)
            return true
        }

        _uiState.update { it.copy(isSaving = false, errorMessage = response.body()?.message ?: "Luu khoanh khac that bai") }
        return false
    }

    fun toggleReaction(momentId: String, reaction: String) {
        val token = accessToken ?: return
        viewModelScope.launch {
            val authHeader = "Bearer $token"
            runCatching {
                apiService.reactToMoment(momentId, MomentReactionRequestDto(reaction), authHeader)
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        applyReactionUpdate(momentId, body)
                    }
                } else {
                    _uiState.update { it.copy(errorMessage = "Khong the cap nhat reaction") }
                }
            }.onFailure { throwable ->
                _uiState.update { it.copy(errorMessage = throwable.message ?: "Khong the cap nhat reaction") }
            }
        }
    }

    fun openComments(momentId: String) {
        val token = accessToken ?: return
        _uiState.update {
            it.copy(
                isCommentsVisible = true,
                isCommentsLoading = true,
                selectedMomentId = momentId,
                comments = emptyList()
            )
        }
        viewModelScope.launch {
            val authHeader = "Bearer $token"
            runCatching {
                apiService.getMomentComments(momentId, authHeader)
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    _uiState.update {
                        it.copy(
                            isCommentsLoading = false,
                            comments = response.body().orEmpty()
                        )
                    }
                } else {
                    _uiState.update { it.copy(isCommentsLoading = false, errorMessage = "Khong the tai binh luan") }
                }
            }.onFailure { throwable ->
                _uiState.update { it.copy(isCommentsLoading = false, errorMessage = throwable.message ?: "Khong the tai binh luan") }
            }
        }
    }

    fun dismissComments() {
        _uiState.update { it.copy(isCommentsVisible = false, selectedMomentId = null, comments = emptyList()) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun submitComment(content: String) {
        val token = accessToken ?: return
        val momentId = _uiState.value.selectedMomentId ?: return
        val safeContent = content.trim()
        if (safeContent.isBlank()) return

        viewModelScope.launch {
            val authHeader = "Bearer $token"
            runCatching {
                apiService.createMomentComment(momentId, MomentCommentRequestDto(safeContent), authHeader)
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    val comment = response.body()
                    if (comment != null) {
                        appendComment(momentId, comment)
                    }
                } else {
                    _uiState.update { it.copy(errorMessage = "Khong the gui binh luan") }
                }
            }.onFailure { throwable ->
                _uiState.update { it.copy(errorMessage = throwable.message ?: "Khong the gui binh luan") }
            }
        }
    }

    private fun applyReactionUpdate(momentId: String, response: MomentReactionResponseDto) {
        _uiState.update { state ->
            val updated = state.moments.map { moment ->
                if (moment.id == momentId) {
                    moment.copy(
                        reactionsCount = response.reactionsCount,
                        viewerReaction = response.viewerReaction,
                        reactions = response.reactions
                    )
                } else {
                    moment
                }
            }
            state.copy(moments = updated)
        }
    }

    private fun appendComment(momentId: String, comment: MomentCommentDto) {
        _uiState.update { state ->
            val updatedComments = state.comments + comment
            val updatedMoments = state.moments.map { moment ->
                if (moment.id == momentId) {
                    moment.copy(commentsCount = moment.commentsCount + 1)
                } else {
                    moment
                }
            }
            state.copy(comments = updatedComments, moments = updatedMoments)
        }
    }

    private suspend fun resolveCoupleId(token: String): String? {
        val cached = authSessionStore.load()?.coupleId
        if (!cached.isNullOrBlank()) return cached
        return runCatching { onboardingRepository.getCoupleStatus(token).coupleId }.getOrNull()
    }

    private fun updateWidgetLatest(moment: MomentDto?) {
        viewModelScope.launch(Dispatchers.IO) {
            MomentWidgetUpdater.updateLatest(appContext, moment)
        }
    }
}

data class MemoriesUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val moments: List<MomentDto> = emptyList(),
    val coupleId: String? = null,
    val currentUsername: String = "",
    val isCommentsVisible: Boolean = false,
    val isCommentsLoading: Boolean = false,
    val selectedMomentId: String? = null,
    val comments: List<MomentCommentDto> = emptyList(),
)
