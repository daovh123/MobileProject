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
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ViewModel cho màn hình Hồ sơ (Profile) của người dùng.
 *
 * Quản lý business logic:
 * - Tải và hiển thị hồ sơ cá nhân (tên, nickname, ngày sinh, giới tính, email)
 * - Chỉnh sửa hồ sơ với validation đầy đủ (tên bắt buộc, định dạng ngày sinh, email, ...)
 * - Theo dõi trạng thái "dirty" (có thay đổi chưa lưu) để enable/disable nút Lưu
 * - Upload ảnh đại diện (base64)
 * - Quản lý khung ảnh đại diện (avatar frame)
 * - Tải trạng thái ghép đôi và thông tin đối phương
 * - Xử lý session expired (401) để điều hướng về Login
 *
 * Sử dụng [async] để tải hồ sơ và trạng thái ghép đôi song song,
 * giảm thời gian chờ tổng thể.
 *
 * Sử dụng [runCatching] cho tất cả API call vì single-shot requests.
 * [Dispatchers.Default] cho decode base64 bitmap (CPU-intensive task).
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val onboardingRepository: OnboardingRepository,
    private val authSessionStore: AuthSessionStore? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    /**
     * Tải hồ sơ cá nhân và trạng thái ghép đôi song song.
     *
     * @param token JWT access token
     *
     * Sử dụng [async] để chạy 2 request concurrently:
     * 1. getProfile: Lấy thông tin cá nhân + decode avatar base64
     * 2. getCoupleStatus: Lấy trạng thái ghép đôi + partner profile (nếu đã ghép)
     */
    fun loadProfile(token: String) {
        if (token.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Phien dang nhap het han, vui long dang nhap lai") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isLoadingCouple = true, errorMessage = null, coupleError = null) }

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

    /**
     * Tải thông tin tóm tắt hồ sơ đối phương (avatar, tên, ...).
     * Chỉ gọi khi đã ghép đôi (paired = true).
     */
    private fun loadPartnerProfile(token: String) {
        viewModelScope.launch {
            runCatching {
                onboardingRepository.getPartnerProfileSummary(token)
            }.onSuccess { profile ->
                _uiState.update { it.copy(partnerProfile = profile, coupleError = null) }
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

    /**
     * Cập nhật draft (bản nháp) hồ sơ khi người dùng chỉnh sửa form.
     * Tự động so sánh với [initialProfile] để xác định [isDirty].
     *
     * @param fullName Họ tên
     * @param nickName Biệt danh
     * @param birthDate Ngày sinh (format yyyy-MM-dd)
     * @param gender Giới tính ("MALE"/"FEMALE"/"OTHER")
     * @param email Email (mặc định giữ nguyên giá trị hiện tại)
     */
    fun updateDraft(
        fullName: String,
        nickName: String,
        birthDate: String,
        gender: String,
        email: String = _uiState.value.email,
    ) {
        val initial = _uiState.value.initialProfile
        _uiState.update { state ->
            state.copy(
                fullName = fullName,
                nickName = nickName,
                birthDate = birthDate,
                gender = gender,
                email = email,
                isDirty = fullName.trim() != (initial?.fullName?.trim() ?: "") ||
                    nickName.trim() != (initial?.nickName?.trim() ?: "") ||
                    birthDate != (initial?.birthDate ?: "") ||
                    gender != (initial?.gender ?: "") ||
                    email.trim() != (initial?.email?.trim() ?: ""),
            )
        }
    }

    /**
     * Lưu hồ sơ sau khi validate đầy đủ.
     *
     * Validation rules:
     * - Họ tên: bắt buộc, tối đa 100 ký tự
     * - Biệt danh: tối đa 50 ký tự (không bắt buộc)
     * - Ngày sinh: format yyyy-MM-dd, không được ở tương lai
     * - Giới tính: MALE/FEMALE/OTHER
     * - Email: đúng định dạng (không bắt buộc)
     *
     * @param token JWT access token
     */
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

    /**
     * Upload ảnh đại diện mới.
     *
     * @param token JWT access token
     * @param imageBytes Dữ liệu ảnh dưới dạng byte array
     * @param contentType MIME type của ảnh (vd: "image/jpeg")
     */
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

    /**
     * Tải danh sách khung ảnh đại diện khả dụng.
     *
     * @param token JWT access token
     */
    fun loadAvatarFrames(token: String) {
        if (token.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingFrames = true) }
            runCatching {
                onboardingRepository.getAvatarFrames(token)
            }.onSuccess { frames ->
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

    /**
     * Chọn và áp dụng khung ảnh đại diện mới.
     *
     * @param token JWT access token
     * @param frameId ID khung ảnh mới (null để xóa khung)
     */
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

    /** Hiển thị dialog chọn khung ảnh. */
    fun showFrameSelector() {
        _uiState.update { it.copy(showFrameSelector = true) }
    }

    /** Ẩn dialog chọn khung ảnh. */
    fun hideFrameSelector() {
        _uiState.update { it.copy(showFrameSelector = false) }
    }

    /** Xóa lỗi liên quan đến avatar. */
    fun clearAvatarError() {
        _uiState.update { it.copy(avatarUploadError = null) }
    }

    /** Xóa tất cả thông báo lỗi (hồ sơ + ghép đôi). */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null, coupleError = null) }
    }

    /** Đánh dấu đã xử lý thông báo lưu thành công. */
    fun consumeSaveSuccess() {
        _uiState.update { it.copy(saveSuccessMessage = null) }
    }

    /** Đánh dấu đã xử lý trạng thái session hết hạn. */
    fun clearSessionExpired() {
        _uiState.update { it.copy(sessionExpired = false) }
    }

    /**
     * Decode chuỗi base64 data URL thành Bitmap trên background thread.
     * Sử dụng [Dispatchers.Default] vì decode bitmap là CPU-intensive task.
     *
     * @param dataUrl Chuỗi "data:image/...;base64,..."
     * @return Bitmap đã decode, hoặc null nếu data URL không hợp lệ
     */
    private suspend fun decodeBase64DataUrl(dataUrl: String): Bitmap? = withContext(Dispatchers.Default) {
        runCatching {
            val base64 = dataUrl.substringAfter(",", missingDelimiterValue = "")
            if (base64.isBlank()) return@runCatching null
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }.getOrNull()
    }
}

/**
 * Kiểm tra exception có phải do session hết hạn (401 Unauthorized) hay không.
 * Dùng để hiển thị dialog "Phiên đăng nhập hết hạn" thay vì message lỗi chung.
 */
private fun Throwable.isSessionExpiredError(): Boolean {
    val reason = message.orEmpty()
    return this is IllegalStateException && (
        reason.contains("unauthorized", ignoreCase = true) ||
            reason.contains("expired", ignoreCase = true) ||
            reason.contains("401", ignoreCase = true)
        )
}

/**
 * Trạng thái UI cho màn hình Hồ sơ (Profile).
 *
 * @property isLoading True khi đang tải hồ sơ
 * @property isSaving True khi đang lưu hồ sơ
 * @property savedProfile Hồ sơ đã lưu trên server (dùng để reload/reference)
 * @property initialProfile Hồ sơ khi vừa load xong (dùng so sánh isDirty)
 * @property fullName Họ tên đang chỉnh sửa
 * @property nickName Biệt danh đang chỉnh sửa
 * @property birthDate Ngày sinh (format yyyy-MM-dd)
 * @property gender Giới tính ("MALE"/"FEMALE"/"OTHER")
 * @property email Email đang chỉnh sửa
 * @property isDirty True nếu có thay đổi so với [initialProfile] (enable nút Lưu)
 * @property errorMessage Thông báo lỗi chung (validation, API)
 * @property saveSuccessMessage Thông báo lưu thành công
 * @property sessionExpired True nếu session hết hạn, UI hiển thị dialog đăng nhập lại
 * @property isLoadingCouple True khi đang tải trạng thái ghép đôi
 * @property coupleStatus Trạng thái ghép đôi hiện tại
 * @property coupleError Thông báo lỗi khi tải trạng thái ghép đôi
 * @property partnerProfile Thông tin tóm tắt hồ sơ đối phương
 * @property avatarUrl Data URL base64 của ảnh đại diện
 * @property avatarBitmap Bitmap đã decode từ [avatarUrl] để hiển thị
 * @property avatarFrameId ID khung ảnh đại diện đang sử dụng
 * @property isUploadingAvatar True khi đang upload ảnh đại diện
 * @property availableFrames Danh sách khung ảnh khả dụng
 * @property isLoadingFrames True khi đang tải danh sách khung ảnh
 * @property showFrameSelector True khi dialog chọn khung ảnh đang hiển thị
 * @property avatarUploadError Thông báo lỗi liên quan đến avatar/khung ảnh
 */
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
    val isDirty: Boolean = false,
    val errorMessage: String? = null,
    val saveSuccessMessage: String? = null,
    val sessionExpired: Boolean = false,
    val isLoadingCouple: Boolean = false,
    val coupleStatus: CoupleStatus? = null,
    val coupleError: String? = null,
    val partnerProfile: PartnerProfileSummary? = null,
    val avatarUrl: String? = null,
    val avatarBitmap: Bitmap? = null,
    val avatarFrameId: String? = null,
    val isUploadingAvatar: Boolean = false,
    val availableFrames: List<AvatarFrame> = emptyList(),
    val isLoadingFrames: Boolean = false,
    val showFrameSelector: Boolean = false,
    val avatarUploadError: String? = null,
)
