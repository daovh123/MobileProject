package com.example.mobileproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileproject.core.result.Resource
import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.domain.entity.PayoutStatus
import com.example.mobileproject.domain.repository.PayoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state cho màn hình chuyển tiền / rút tiền.
 *
 * @property isSubmitting Đang gửi yêu cầu tạo lệnh rút lên server
 * @property isSuccess Lệnh rút đã hoàn tất thành công (SePay xác nhận tiền ra)
 * @property isWaitingBankConfirmation Đang chờ người dùng chuyển tiền thật qua app ngân hàng
 * @property payoutId ID của lệnh rút trên server
 * @property transferCode Mã giao dịch SePay, người dùng cần dùng mã này khi chuyển tiền thật
 * @property statusText Thông báo trạng thái hiện tại hiển thị cho người dùng
 * @property errorMessage Thông báo lỗi nếu có
 */
data class TransferMoneyUiState(
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val isWaitingBankConfirmation: Boolean = false,
    val payoutId: String? = null,
    val transferCode: String? = null,
    val statusText: String? = null,
    val errorMessage: String? = null,
)

/**
 * ViewModel phục vụ màn hình chuyển tiền / rút tiền từ quỹ chung.
 *
 * Xử lý business logic:
 * - Tạo lệnh rút tiền (payout) thông qua [PayoutRepository]
 * - Polling trạng thái lệnh rút từ server để xác nhận SePay webhook tiền ra
 * - Quản lý trạng thái UI: đang gửi, chờ xác nhận ngân hàng, thành công/thất bại
 *
 * Luồng hoạt động:
 * 1. Người dùng nhập thông tin và submit → gọi API tạo payout
 * 2. Nhận transferCode, hiển thị hướng dẫn chuyển tiền thật
 * 3. Polling mỗi 3 giây, tối đa 20 lần (60 giây) để chờ SePay xác nhận
 * 4. Cập nhật UI khi trạng thái payout thay đổi (PAID/FAILED)
 */
@HiltViewModel
class TransferMoneyViewModel @Inject constructor(
    private val payoutRepository: PayoutRepository,
    private val authSessionStore: AuthSessionStore,
) : ViewModel() {

    // StateFlow pattern: MutableStateFlow nội bộ + expose read-only asStateFlow
    private val _uiState = MutableStateFlow(TransferMoneyUiState())
    val uiState: StateFlow<TransferMoneyUiState> = _uiState.asStateFlow()

    /**
     * Gửi yêu cầu tạo lệnh rút tiền.
     *
     * Validates coupleId từ session và số tiền > 0 trước khi gọi API.
     * Sau khi tạo payout thành công, bắt đầu polling trạng thái.
     *
     * @param amount Số tiền cần rút (đơn vị: VND, phải > 0)
     * @param bankName Tên ngân hàng (hiển thị trong hướng dẫn)
     * @param accountNumber Số tài khoản ngân hàng (hiển thị trong hướng dẫn)
     * @param note Ghi chú thêm cho giao dịch (có thể null)
     */
    fun submitTransfer(
        amount: Long,
        bankName: String,
        accountNumber: String,
        note: String?,
    ) {
        val session = authSessionStore.load()
        val coupleId = session?.coupleId
        if (coupleId.isNullOrBlank()) {
            _uiState.update { it.copy(errorMessage = "Không tìm thấy thông tin cặp đôi để tạo lệnh rút.") }
            return
        }
        if (amount <= 0L) {
            _uiState.update { it.copy(errorMessage = "Số tiền rút phải lớn hơn 0.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSubmitting = true,
                    isSuccess = false,
                    isWaitingBankConfirmation = false,
                    payoutId = null,
                    transferCode = null,
                    statusText = null,
                    errorMessage = null,
                )
            }

            when (val result = payoutRepository.createPayout(coupleId = coupleId, amount = amount)) {
                is Resource.Success -> {
                    val payout = result.data
                    val summary = buildString {
                        append("Đã tạo lệnh rút ")
                        append(payout.transferCode)
                        append(". Chuyển tiền thật theo thông tin ngân hàng và mã này.")
                        if (!bankName.isBlank() && !accountNumber.isBlank()) {
                            append(" (")
                            append(bankName)
                            append(" - ")
                            append(accountNumber)
                            append(")")
                        }
                        if (!note.isNullOrBlank()) {
                            append(" | ")
                            append(note.trim())
                        }
                    }
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            isWaitingBankConfirmation = true,
                            payoutId = payout.id,
                            transferCode = payout.transferCode,
                            statusText = "$summary Đang chờ bạn chuyển tiền thật trong app ngân hàng, sau đó SePay xác nhận webhook tiền ra.",
                        )
                    }
                    pollPayoutStatus(payout.id)
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = result.throwable.message ?: "Không thể tạo yêu cầu rút tiền.",
                        )
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    /**
     * Polling trạng thái lệnh rút tiền từ server.
     *
     * Gọi API getPayout mỗi 3 giây, tối đa 20 lần (tổng ~60 giây).
     * Dừng ngay khi nhận được trạng thái PAID hoặc FAILED.
     * Nếu hết số lần poll mà vẫn chưa xác nhận, UI hiển thị trạng thái chờ.
     *
     * @param payoutId ID lệnh rút cần theo dõi trạng thái
     */
    private suspend fun pollPayoutStatus(payoutId: String) {
        repeat(20) {
            delay(3000L)
            when (val result = payoutRepository.getPayout(payoutId)) {
                is Resource.Success -> {
                    when (result.data.status) {
                        PayoutStatus.PAID -> {
                            _uiState.update {
                                it.copy(
                                    isWaitingBankConfirmation = false,
                                    isSuccess = true,
                                    statusText = "Rút tiền thành công. Quỹ đã được cập nhật.",
                                )
                            }
                            return
                        }
                        PayoutStatus.FAILED -> {
                            _uiState.update {
                                it.copy(
                                    isWaitingBankConfirmation = false,
                                    statusText = "Lệnh rút tiền thật thất bại.",
                                    errorMessage = "SePay không xác nhận được giao dịch tiền ra.",
                                )
                            }
                            return
                        }
                        else -> {
                            _uiState.update {
                                it.copy(statusText = "Đang chờ bạn chuyển tiền thật trong app ngân hàng và SePay xác nhận giao dịch tiền ra...")
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(statusText = "Đã tạo lệnh rút, đang đồng bộ trạng thái...")
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    /**
     * Xóa thông báo lỗi hiện tại trong UI state.
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
