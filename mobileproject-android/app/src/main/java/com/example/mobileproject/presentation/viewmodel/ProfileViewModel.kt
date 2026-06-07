package com.example.mobileproject.presentation.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.domain.entity.AvatarFrame
import com.example.mobileproject.domain.entity.CoupleStatus
import com.example.mobileproject.domain.entity.PartnerProfileSummary
import com.example.mobileproject.domain.entity.ProfileResult
import com.example.mobileproject.domain.repository.OnboardingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val onboardingRepository: OnboardingRepository,
    private val authSessionStore: AuthSessionStore? = null,
) : ViewModel() {

    private companion object {
        const val PROFILE_STALE_MS: Long = 60_000L
        const val AVATAR_FRAMES_STALE_MS: Long = 5 * 60_000L
    }

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    private var lastProfileToken: String? = null
    private var lastProfileLoadedAt: Long = 0L
    private var lastFramesToken: String? = null
    private var lastFramesLoadedAt: Long = 0L
    private var loadProfileJob: Job? = null

    fun ensureProfileLoaded(token: String, force: Boolean = false) {
        val current = _uiState.value
        val hasCachedProfile = current.savedProfile != null
        val hasCachedCouple = current.coupleStatus != null
        val sameToken = lastProfileToken == token.trim()
        val isFresh = System.currentTimeMillis() - lastProfileLoadedAt < PROFILE_STALE_MS

        if (!force && sameToken && hasCachedProfile && hasCachedCouple && isFresh) {
            return
        }

        loadProfile(token)
    }

    fun refreshProfileIfStale(token: String) {
        ensureProfileLoaded(token, force = false)
    }

    fun loadProfile(token: String) {
        if (token.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Phien dang nhap het han, vui long dang nhap lai") }
            return
        }

        loadProfileJob?.cancel()
        loadProfileJob = viewModelScope.launch {
            val current = _uiState.value
            _uiState.update {
                it.copy(
                    isLoading = current.savedProfile == null,
                    isLoadingCouple = current.coupleStatus == null,
                    errorMessage = null,
                    coupleError = null,
                )
            }

            val profileDeferred = async {
                runCatching { onboardingRepository.getProfile(token) }
            }
            val coupleDeferred = async {
                runCatching { onboardingRepository.getCoupleStatus(token) }
            }

            val profileResult = profileDeferred.await()
            val coupleResult = coupleDeferred.await()

            profileResult.onSuccess { profile ->
                val bitmap = profile.avatarUrl?.let { decodeBase64DataUrl(it) }
                lastProfileToken = token.trim()
                lastProfileLoadedAt = System.currentTimeMillis()
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        savedProfile = profile,
                        initialProfile = profile,
                        fullName = profile.fullName ?: "",
                        nickName = profile.nickName ?: "",
                        birthDate = profile.birthDate ?: "",
                        gender = profile.gender ?: "",
                        email = profile.email ?: "",
                        phoneNumber = profile.phoneNumber ?: "",
                        isDirty = false,
                        avatarUrl = profile.avatarUrl,
                        avatarBitmap = bitmap,
                        avatarFrameId = profile.avatarFrameId,
                    )
                }
            }.onFailure { error ->
                val sessionExpired = error.isSessionExpiredError()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        sessionExpired = sessionExpired,
                        errorMessage = if (sessionExpired) null else (error.message ?: "Tai ho so that bai"),
                    )
                }
            }

            coupleResult.onSuccess { status ->
                lastProfileToken = token.trim()
                lastProfileLoadedAt = System.currentTimeMillis()
                _uiState.update { it.copy(isLoadingCouple = false, coupleStatus = status) }
                if (status.paired) {
                    loadPartnerProfile(token)
                } else {
                    _uiState.update { it.copy(partnerProfile = null) }
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoadingCouple = false,
                        coupleError = error.message,
                        partnerProfile = null,
                    )
                }
            }
        }
    }

    private fun loadPartnerProfile(token: String) {
        viewModelScope.launch {
            runCatching {
                onboardingRepository.getPartnerProfileSummary(token)
            }.onSuccess { profile ->
                val bitmap = profile.avatarUrl?.let { decodeBase64DataUrl(it) }
                _uiState.update {
                    it.copy(
                        partnerProfile = profile,
                        partnerAvatarBitmap = bitmap,
                        coupleError = null
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        partnerProfile = null,
                        coupleError = error.message ?: "Khong the tai thong tin doi phuong",
                    )
                }
            }
        }
    }

    fun updateDraft(
        fullName: String,
        nickName: String,
        birthDate: String,
        gender: String,
        email: String = _uiState.value.email,
        phoneNumber: String = _uiState.value.phoneNumber,
    ) {
        val initial = _uiState.value.initialProfile
        _uiState.update { state ->
            state.copy(
                fullName = fullName,
                nickName = nickName,
                birthDate = birthDate,
                gender = gender,
                email = email,
                phoneNumber = phoneNumber,
                isDirty = fullName.trim() != (initial?.fullName?.trim() ?: "") ||
                    nickName.trim() != (initial?.nickName?.trim() ?: "") ||
                    birthDate != (initial?.birthDate ?: "") ||
                    gender != (initial?.gender ?: "") ||
                    email.trim() != (initial?.email?.trim() ?: "") ||
                    phoneNumber.trim() != (initial?.phoneNumber?.trim() ?: ""),
            )
        }
    }

    fun saveProfile(token: String) {
        if (token.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Phien dang nhap het han, vui long dang nhap lai") }
            return
        }

        val state = _uiState.value
        val fullName = state.fullName
        val nickName = state.nickName
        val birthDate = state.birthDate
        val gender = state.gender
        val email = state.email

        // Validation
        if (fullName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Ho ten khong duoc de trong") }
            return
        }
        if (fullName.trim().length > 100) {
            _uiState.update { it.copy(errorMessage = "Ho ten qua 100 ky tu") }
            return
        }
        if (nickName.isNotBlank() && nickName.trim().length > 50) {
            _uiState.update { it.copy(errorMessage = "Bi danh qua 50 ky tu") }
            return
        }
        if (!birthDate.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))) {
            _uiState.update { it.copy(errorMessage = "Ngay sinh khong hop le") }
            return
        }
        val parsedDate = runCatching { java.time.LocalDate.parse(birthDate) }.getOrNull()
        if (parsedDate == null) {
            _uiState.update { it.copy(errorMessage = "Ngay sinh khong hop le") }
            return
        }
        if (parsedDate.isAfter(java.time.LocalDate.now())) {
            _uiState.update { it.copy(errorMessage = "Ngay sinh khong duoc o tuong lai") }
            return
        }
        if (gender !in listOf("MALE", "FEMALE", "OTHER")) {
            _uiState.update { it.copy(errorMessage = "Gioi tinh khong hop le") }
            return
        }
        if (email.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            _uiState.update { it.copy(errorMessage = "Email khong hop le") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                onboardingRepository.saveProfile(
                    token = token,
                    fullName = fullName.trim(),
                    nickName = nickName.trim().takeIf { it.isNotBlank() },
                    birthDate = birthDate.trim(),
                    gender = gender.trim(),
                    email = email.trim().takeIf { it.isNotBlank() },
                    phoneNumber = state.phoneNumber.trim().takeIf { it.isNotBlank() },
                )
            }.onSuccess { profile ->
                authSessionStore?.updateProfileState(
                    profileCompleted = profile.profileCompleted,
                    coupleConnected = profile.coupleConnected,
                )
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        savedProfile = profile,
                        initialProfile = profile,
                        isDirty = false,
                        saveSuccessMessage = "Luu ho so thanh cong",
                    )
                }
            }.onFailure { error ->
                val sessionExpired = error.isSessionExpiredError()
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        sessionExpired = sessionExpired,
                        errorMessage = if (sessionExpired) null else (error.message ?: "Luu ho so that bai"),
                    )
                }
            }
        }
    }

    fun uploadAvatar(token: String, imageBytes: ByteArray, contentType: String) {
        if (token.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingAvatar = true, avatarUploadError = null) }
            runCatching {
                onboardingRepository.uploadAvatar(token, imageBytes, contentType)
            }.onSuccess { dataUrl ->
                val bitmap = decodeBase64DataUrl(dataUrl)
                _uiState.update { it.copy(isUploadingAvatar = false, avatarUrl = dataUrl, avatarBitmap = bitmap) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isUploadingAvatar = false,
                        avatarUploadError = error.message ?: "Tai anh that bai",
                    )
                }
            }
        }
    }

    fun loadAvatarFrames(token: String) {
        if (token.isBlank()) return
        val trimmedToken = token.trim()
        val hasFrames = _uiState.value.availableFrames.isNotEmpty()
        val isFresh = System.currentTimeMillis() - lastFramesLoadedAt < AVATAR_FRAMES_STALE_MS
        if (hasFrames && trimmedToken == lastFramesToken && isFresh) {
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingFrames = true) }
            runCatching {
                onboardingRepository.getAvatarFrames(token)
            }.onSuccess { frames ->
                lastFramesToken = trimmedToken
                lastFramesLoadedAt = System.currentTimeMillis()
                _uiState.update { it.copy(isLoadingFrames = false, availableFrames = frames) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoadingFrames = false,
                        avatarUploadError = error.message ?: "Khong the tai danh sach khung anh",
                    )
                }
            }
        }
    }

    fun selectFrame(token: String, frameId: String?) {
        if (token.isBlank()) return
        viewModelScope.launch {
            runCatching {
                onboardingRepository.setAvatarFrame(token, frameId)
            }.onSuccess { profile ->
                _uiState.update {
                    it.copy(
                        avatarFrameId = profile.avatarFrameId,
                        showFrameSelector = false,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(avatarUploadError = error.message ?: "Dat khung anh that bai")
                }
            }
        }
    }

    fun showFrameSelector() {
        _uiState.update { it.copy(showFrameSelector = true) }
    }

    fun hideFrameSelector() {
        _uiState.update { it.copy(showFrameSelector = false) }
    }

    fun clearAvatarError() {
        _uiState.update { it.copy(avatarUploadError = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null, coupleError = null) }
    }

    fun consumeSaveSuccess() {
        _uiState.update { it.copy(saveSuccessMessage = null) }
    }

    fun clearSessionExpired() {
        _uiState.update { it.copy(sessionExpired = false) }
    }

    private suspend fun decodeBase64DataUrl(dataUrl: String): Bitmap? = withContext(Dispatchers.Default) {
        runCatching {
            val base64 = dataUrl.substringAfter(",", missingDelimiterValue = "")
            if (base64.isBlank()) return@runCatching null
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }.getOrNull()
    }
}

private fun Throwable.isSessionExpiredError(): Boolean {
    val reason = message.orEmpty()
    return this is IllegalStateException && (
        reason.contains("unauthorized", ignoreCase = true) ||
            reason.contains("expired", ignoreCase = true) ||
            reason.contains("401", ignoreCase = true)
        )
}

data class ProfileUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val savedProfile: ProfileResult? = null,
    val initialProfile: ProfileResult? = null,
    val fullName: String = "",
    val nickName: String = "",
    val birthDate: String = "",
    val gender: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val isDirty: Boolean = false,
    val errorMessage: String? = null,
    val saveSuccessMessage: String? = null,
    val sessionExpired: Boolean = false,
    val isLoadingCouple: Boolean = false,
    val coupleStatus: CoupleStatus? = null,
    val coupleError: String? = null,
    val partnerProfile: PartnerProfileSummary? = null,
    val partnerAvatarBitmap: Bitmap? = null,
    val avatarUrl: String? = null,
    val avatarBitmap: Bitmap? = null,
    val avatarFrameId: String? = null,
    val isUploadingAvatar: Boolean = false,
    val availableFrames: List<AvatarFrame> = emptyList(),
    val isLoadingFrames: Boolean = false,
    val showFrameSelector: Boolean = false,
    val avatarUploadError: String? = null,
)
