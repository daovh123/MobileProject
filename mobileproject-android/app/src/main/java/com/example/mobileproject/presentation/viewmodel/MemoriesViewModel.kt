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
import com.example.mobileproject.domain.entity.PartnerProfileSummary
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

/**
 * ViewModel cho màn hình Khoảnh khắc (Memories/Moments) của cặp đôi.
 *
 * Quản lý business logic:
 * - Tải danh sách khoảnh khắc (moments) của cặp đôi
 * - Tạo khoảnh khắc mới với ảnh base64
 * - Toggle reaction (like, love, ...) trên khoảnh khắc
 * - Quản lý bình luận (mở/đóng, tải, gửi bình luận)
 * - Tải thông tin đối phương (profile summary)
 * - Cập nhật widget hiển thị khoảnh khắc mới nhất
 *
 * Sử dụng [runCatching] cho tất cả API call vì single-shot requests.
 * Response từ API được kiểm tra cả [isSuccessful] (HTTP status) và body.success (business logic).
 *
 * Widget update chạy trên [Dispatchers.IO] vì có thể ghi SharedPreferences.
 */
@HiltViewModel
class MemoriesViewModel @Inject constructor(
    private val apiService: ApiService,
    private val onboardingRepository: OnboardingRepository,
    private val authSessionStore: AuthSessionStore,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MemoriesUiState())
    val uiState: StateFlow<MemoriesUiState> = _uiState.asStateFlow()

    /** Access token lưu tạm để dùng cho các API call trong session này. */
    private var accessToken: String? = null

    /**
     * Khởi tạo: lưu token, tải thông tin cặp đôi (ngày bắt đầu, username đối phương),
     * sau đó tải danh sách khoảnh khắc.
     *
     * @param accessToken JWT access token
     */
    fun load(accessToken: String) {
        if (accessToken.isBlank()) return
        this.accessToken = accessToken.trim()
        val session = authSessionStore.load()
        _uiState.update { it.copy(currentUsername = session?.username.orEmpty()) }
        viewModelScope.launch {
            runCatching { onboardingRepository.getCoupleStatus(this@MemoriesViewModel.accessToken.orEmpty()) }
                .onSuccess { status ->
                    _uiState.update {
                        it.copy(
                            relationshipStartAt = status.startAt,
                            partnerUsername = status.partnerUsername,
                        )
                    }
                }
        }
        refreshMoments()
    }

    /**
     * Tải lại danh sách khoảnh khắc từ API.
     *
     * @param showLoading True để hiển thị loading indicator (lần đầu/tự refresh),
     *   False khi refresh ngầm sau khi tạo mới
     */
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

    /**
     * Tạo khoảnh khắc mới với ảnh base64.
     * Sau khi tạo thành công, thêm moment mới vào đầu danh sách và refresh ngầm.
     *
     * @param title Tiêu đề khoảnh khắc
     * @param base64Image Ảnh mã hóa base64
     * @return True nếu tạo thành công
     */
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

    /**
     * Toggle reaction (like, love, haha, ...) trên khoảnh khắc.
     * Nếu đã reaction cùng loại thì bỏ reaction, nếu khác loại thì đổi.
     *
     * @param momentId ID khoảnh khắc
     * @param reaction Loại reaction (vd: "like", "love")
     */
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

    /**
     * Mở panel bình luận cho khoảnh khắc cụ thể.
     * Tải danh sách bình luận từ API.
     *
     * @param momentId ID khoảnh khắc cần xem bình luận
     */
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

    /** Đóng panel bình luận và xóa dữ liệu bình luận đang hiển thị. */
    fun dismissComments() {
        _uiState.update { it.copy(isCommentsVisible = false, selectedMomentId = null, comments = emptyList()) }
    }

    /** Xóa thông báo lỗi. */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Toggle hiển thị thông tin đối phương.
     * Nếu expand và chưa có profile, tự động tải từ API.
     * Sử dụng lazy loading: chỉ gọi API lần đầu expand.
     */
    fun onPartnerInfoClick() {
        val token = accessToken ?: return
        val current = _uiState.value
        val willExpand = !current.isPartnerInfoExpanded

        _uiState.update { it.copy(isPartnerInfoExpanded = willExpand) }
        if (!willExpand || current.isPartnerInfoLoading) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isPartnerInfoLoading = true, errorMessage = null) }
            runCatching {
                onboardingRepository.getPartnerProfileSummary(token)
            }.onSuccess { profile ->
                _uiState.update {
                    it.copy(
                        isPartnerInfoLoading = false,
                        partnerProfile = profile,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isPartnerInfoLoading = false,
                        errorMessage = throwable.message ?: "Khong the tai thong tin doi phuong",
                    )
                }
            }
        }
    }

    /**
     * Gửi bình luận mới cho khoảnh khắc đang chọn.
     * Sau khi gửi thành công, thêm comment vào danh sách và tăng commentsCount.
     *
     * @param content Nội dung bình luận
     */
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

    /**
     * Cập nhật reaction locally (optimistic update) sau khi server trả về kết quả.
     */
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

    /**
     * Thêm bình luận mới vào danh sách và tăng commentsCount của moment tương ứng.
     */
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

    /**
     * Lấy coupleId từ cache, nếu chưa có thì gọi API.
     */
    private suspend fun resolveCoupleId(token: String): String? {
        val cached = authSessionStore.load()?.coupleId
        if (!cached.isNullOrBlank()) return cached
        return runCatching { onboardingRepository.getCoupleStatus(token).coupleId }.getOrNull()
    }

    /**
     * Cập nhật widget với khoảnh khắc mới nhất.
     * Chạy trên [Dispatchers.IO] vì có thể ghi SharedPreferences.
     */
    private fun updateWidgetLatest(moment: MomentDto?) {
        // Chạy trên Dispatchers.IO vì widget update có thể ghi SharedPreferences
        viewModelScope.launch(Dispatchers.IO) {
            MomentWidgetUpdater.updateLatest(appContext, moment)
        }
    }
}

/**
 * Trạng thái UI cho màn hình Khoảnh khắc (Memories).
 *
 * @property isLoading True khi đang tải danh sách khoảnh khắc
 * @property isSaving True khi đang tạo khoảnh khắc mới
 * @property errorMessage Thông báo lỗi
 * @property moments Danh sách khoảnh khắc (MomentDto từ API)
 * @property coupleId ID cặp đôi (null nếu chưa ghép đôi)
 * @property currentUsername Username người dùng hiện tại (dùng phân biệt "của mình" vs "của đối phương")
 * @property isCommentsVisible True khi panel bình luận đang hiển thị
 * @property isCommentsLoading True khi đang tải danh sách bình luận
 * @property selectedMomentId ID khoảnh khắc đang xem bình luận
 * @property comments Danh sách bình luận của khoảnh khắc đang chọn
 * @property isPartnerInfoExpanded True khi section thông tin đối phương đang mở rộng
 * @property isPartnerInfoLoading True khi đang tải profile đối phương
 * @property partnerProfile Thông tin tóm tắt hồ sơ đối phương
 * @property relationshipStartAt Ngày bắt đầu mối quan hệ (hiển thị "X ngày bên nhau")
 * @property partnerUsername Username đối phương
 */
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
    val isPartnerInfoExpanded: Boolean = false,
    val isPartnerInfoLoading: Boolean = false,
    val partnerProfile: PartnerProfileSummary? = null,
    val relationshipStartAt: String? = null,
    val partnerUsername: String? = null,
)
