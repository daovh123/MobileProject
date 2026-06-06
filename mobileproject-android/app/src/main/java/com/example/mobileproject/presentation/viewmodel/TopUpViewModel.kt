package com.example.mobileproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileproject.R
import com.example.mobileproject.core.result.Resource
import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.domain.entity.TopUpRequest
import com.example.mobileproject.domain.entity.TopUpStatus
import com.example.mobileproject.domain.repository.TopUpRepository
import com.example.mobileproject.domain.repository.WalletRepository
import com.example.mobileproject.presentation.model.wallet.VietnamBank
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Bước hiện tại trong flow nạp tiền:
 * - [AMOUNT]: Nhập số tiền
 * - [BANK_SELECT]: Chọn ngân hàng
 */
enum class TopUpStep {
    AMOUNT,
    BANK_SELECT,
}

/**
 * Phương thức thanh toán nạp tiền:
 * - [QR]: Quét mã QR
 * - [BANK_REDIRECT]: Chuyển hướng đến app/web ngân hàng
 */
enum class TopUpPaymentMode {
    QR,
    BANK_REDIRECT,
}

/**
 * Sự kiện điều hướng one-shot từ TopUpViewModel.
 * Sử dụng [SharedFlow] thay vì StateFlow để mỗi event chỉ được xử lý một lần.
 *
 * - [OpenQr]: Mở màn hình QR code để quét thanh toán
 * - [OpenBankRedirect]: Mở trang web/app ngân hàng để chuyển khoản
 */
sealed interface TopUpNavigationEvent {
    data class OpenQr(val topUpId: String) : TopUpNavigationEvent
    data class OpenBankRedirect(val topUpId: String) : TopUpNavigationEvent
}

/**
 * Trạng thái UI cho màn hình Nạp tiền (TopUp).
 *
 * @property amount Số tiền nhập vào (chuỗi số)
 * @property note Ghi chú nạp tiền
 * @property destination Hiển thị đích đến nạp tiền ("Nạp vào Ví chính")
 * @property currentBalance Số dư ví hiện tại (cập nhật sau khi nạp thành công)
 * @property isLoading True khi đang tạo yêu cầu nạp tiền
 * @property isPolling True khi đang polling trạng thái nạp tiền
 * @property isSuccess True khi nạp tiền thành công (status = PAID)
 * @property error Thông báo lỗi
 * @property currentStep Bước hiện tại trong flow nạp tiền
 * @property selectedBank Ngân hàng đã chọn (null nếu chưa chọn)
 * @property activeTopUp Yêu cầu nạp tiền đang active (chờ thanh toán hoặc đã hoàn tất)
 */
data class TopUpUiState(
    val amount: String = "",
    val note: String = "",
    val destination: String = "Nạp vào Ví chính",
    val currentBalance: Long = 0L,
    val isLoading: Boolean = false,
    val isPolling: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val currentStep: TopUpStep = TopUpStep.AMOUNT,
    val selectedBank: VietnamBank? = null,
    val activeTopUp: TopUpRequest? = null,
) {
    /** Số dư dự kiến sau khi nạp: currentBalance + amount. */
    val predictedBalance: Long
        get() {
            val addAmount = amount.toLongOrNull() ?: 0L
            return currentBalance + addAmount
        }

    /** True nếu số tiền hợp lệ (> 0). */
    val isAmountValid: Boolean
        get() = (amount.toLongOrNull() ?: 0L) > 0
}

/**
 * ViewModel cho màn hình Nạp tiền (TopUp) vào ví chung.
 *
 * Quản lý business logic:
 * - Multi-step flow: Nhập số tiền -> Chọn ngân hàng
 * - Tạo yêu cầu nạp tiền qua API
 * - Polling trạng thái nạp tiền mỗi 3 giây cho đến khi PAID/FAILED/EXPIRED
 * - Điều hướng đến màn hình QR hoặc redirect ngân hàng (one-shot event)
 * - Cập nhật số dư ví local khi nạp thành công
 *
 * Navigation events sử dụng [SharedFlow] (replay=0) thay vì StateFlow
 * để mỗi event chỉ được consume một lần, tránh re-trigger khi recompose.
 *
 * Polling sử dụng while loop + delay, dừng khi status != PENDING
 * (tức PAID, FAILED, hoặc EXPIRED). Job được cancel trong [onCleared].
 */
@HiltViewModel
class TopUpViewModel @Inject constructor(
    private val topUpRepository: TopUpRepository,
    private val walletRepository: WalletRepository,
    private val authSessionStore: AuthSessionStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TopUpUiState())
    val uiState: StateFlow<TopUpUiState> = _uiState.asStateFlow()

    /** Navigation events one-shot (QR hoặc bank redirect). */
    private val _navigationEvents = MutableSharedFlow<TopUpNavigationEvent>()
    val navigationEvents: SharedFlow<TopUpNavigationEvent> = _navigationEvents.asSharedFlow()

    /** Job polling trạng thái nạp tiền. */
    private var pollingJob: Job? = null

    init {
        // Tải số dư ví hiện tại khi ViewModel được tạo
        loadCurrentBalance()
    }

    /**
     * Tải số dư ví chung hiện tại từ repository.
     * Sử dụng Flow collect vì wallet repository trả về realtime Flow.
     */
    private fun loadCurrentBalance() {
        val session = authSessionStore.load()
        val coupleId = session?.coupleId ?: return
        val token = session.token

        viewModelScope.launch {
            walletRepository.getWallet(coupleId, token).collect { result ->
                result.onSuccess { wallet ->
                    _uiState.update { it.copy(currentBalance = wallet.balance) }
                }
            }
        }
    }

    /**
     * Cập nhật số tiền nạp (chỉ chấp nhận ký tự số).
     *
     * @param newAmount Chuỗi số tiền mới
     */
    fun onAmountChange(newAmount: String) {
        if (newAmount.all { it.isDigit() }) {
            _uiState.update { it.copy(amount = newAmount) }
        }
    }

    /**
     * Cập nhật ghi chú nạp tiền.
     *
     * @param newNote Chuỗi ghi chú
     */
    fun onNoteChange(newNote: String) {
        _uiState.update { it.copy(note = newNote) }
    }

    /**
     * Chọn ngân hàng để nạp tiền.
     *
     * @param bank Thông tin ngân hàng đã chọn
     */
    fun onBankSelected(bank: VietnamBank) {
        _uiState.update { it.copy(selectedBank = bank) }
    }

    /**
     * Chuyển sang bước tiếp theo (Amount -> Bank Select).
     * Chỉ chuyển nếu số tiền hợp lệ.
     */
    fun goToNextStep() {
        val state = _uiState.value
        if (state.currentStep == TopUpStep.AMOUNT && state.isAmountValid) {
            _uiState.update { it.copy(currentStep = TopUpStep.BANK_SELECT) }
        }
    }

    /**
     * Quay lại bước trước (Bank Select -> Amount).
     * Reset ngân hàng đã chọn.
     */
    fun goToPreviousStep() {
        val state = _uiState.value
        if (state.currentStep == TopUpStep.BANK_SELECT) {
            _uiState.update { it.copy(currentStep = TopUpStep.AMOUNT, selectedBank = null) }
        }
    }

    /**
     * Tạo yêu cầu nạp tiền và điều hướng đến màn hình thanh toán.
     *
     * @param paymentMode Phương thức thanh toán (QR hoặc bank redirect)
     *
     * Flow: Validate -> Gọi API tạo top-up -> Emit navigation event
     * Navigation event là one-shot (SharedFlow replay=0) để mỗi lần chỉ xử lý một lần.
     */
    fun createTopUpRequest(paymentMode: TopUpPaymentMode) {
        val state = _uiState.value
        val amountLong = state.amount.toLongOrNull() ?: 0L
        if (amountLong <= 0) {
            _uiState.update { it.copy(error = "Vui lòng nhập số tiền hợp lệ") }
            return
        }

        val bank = state.selectedBank
        if (bank == null) {
            _uiState.update { it.copy(error = "Vui lòng chọn ngân hàng") }
            return
        }

        val session = authSessionStore.load()
        val coupleId = session?.coupleId
        if (coupleId.isNullOrBlank()) {
            _uiState.update { it.copy(error = "Chưa tìm thấy ví chung của hai bạn") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, isSuccess = false) }
            val result = topUpRepository.createTopUp(
                coupleId = coupleId,
                amount = amountLong,
                bankId = bank.id,
                bankName = bank.name,
                note = state.note.ifBlank { "Nạp tiền ví You & Me" },
            )

            when (result) {
                is Resource.Success -> {
                    handleTopUpUpdate(result.data)
                    _uiState.update { it.copy(isLoading = false) }
                    when (paymentMode) {
                        TopUpPaymentMode.QR -> _navigationEvents.emit(TopUpNavigationEvent.OpenQr(result.data.id))
                        TopUpPaymentMode.BANK_REDIRECT -> {
                            _navigationEvents.emit(TopUpNavigationEvent.OpenBankRedirect(result.data.id))
                        }
                    }
                }

                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.throwable.message ?: "Không tạo được yêu cầu nạp tiền",
                        )
                    }
                }

                else -> _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Bắt đầu polling trạng thái nạp tiền mỗi [POLL_INTERVAL_MS] (3 giây).
     *
     * Dừng polling khi:
     * - status != PENDING (tức PAID, FAILED, EXPIRED)
     * - Gặp lỗi API
     * - Job bị cancel (ViewModel destroyed)
     *
     * @param topUpId ID yêu cầu nạp tiền cần theo dõi
     */
    fun startTopUpStatusPolling(topUpId: String) {
        if (topUpId.isBlank()) {
            _uiState.update { it.copy(error = "Thiếu mã yêu cầu nạp tiền") }
            return
        }

        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            _uiState.update { it.copy(isPolling = true, error = null) }
            var keepPolling = true

            // Polling loop: tiếp tục cho đến khi status != PENDING
            while (keepPolling) {
                keepPolling = when (val result = topUpRepository.getTopUp(topUpId)) {
                    is Resource.Success -> {
                        handleTopUpUpdate(result.data)
                        result.data.status == TopUpStatus.PENDING
                    }

                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(error = result.throwable.message ?: "Không kiểm tra được trạng thái nạp tiền")
                        }
                        false
                    }

                    Resource.Loading -> true
                }

                if (keepPolling) {
                    delay(POLL_INTERVAL_MS) // Chờ 3 giây trước lần poll tiếp theo
                }
            }

            _uiState.update { it.copy(isPolling = false) }
        }
    }

    /**
     * Refresh trạng thái nạp tiền một lần (không polling).
     *
     * @param topUpId ID yêu cầu nạp tiền
     */
    fun refreshTopUpStatus(topUpId: String) {
        if (topUpId.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = topUpRepository.getTopUp(topUpId)) {
                is Resource.Success -> handleTopUpUpdate(result.data)
                is Resource.Error -> _uiState.update {
                    it.copy(error = result.throwable.message ?: "Không kiểm tra được trạng thái nạp tiền")
                }
                Resource.Loading -> Unit
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    /**
     * Xử lý kết quả nạp tiền từ API.
     * Nếu PAID, cập nhật số dư ví local.
     * Nếu FAILED/EXPIRED, hiển thị thông báo lỗi tương ứng.
     */
    private suspend fun handleTopUpUpdate(topUp: TopUpRequest) {
        if (topUp.status == TopUpStatus.PAID && topUp.currentBalance != null) {
            walletRepository.updateLocalBalance(topUp.currentBalance)
        }

        _uiState.update {
            it.copy(
                activeTopUp = topUp,
                currentBalance = topUp.currentBalance ?: it.currentBalance,
                isSuccess = topUp.status == TopUpStatus.PAID,
                error = when (topUp.status) {
                    TopUpStatus.FAILED -> "Giao dịch nạp tiền thất bại"
                    TopUpStatus.EXPIRED -> "Yêu cầu nạp tiền đã hết hạn"
                    else -> it.error
                },
            )
        }
    }

    /** Xóa thông báo lỗi. */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Hủy polling job khi ViewModel bị destroy.
     */
    override fun onCleared() {
        pollingJob?.cancel()
        super.onCleared()
    }

    companion object {
        /** Khoảng thời gian giữa các lần polling trạng thái nạp tiền (3 giây). */
        private const val POLL_INTERVAL_MS = 3_000L

        /** Danh sách tất cả ngân hàng Việt Nam hỗ trợ nạp tiền. */
        val vietnamBanks = listOf(
            VietnamBank("abbank", "ABBank", "AB", R.drawable.bank_logo_abbank),
            VietnamBank("acb", "ACB", "ACB", R.drawable.bank_logo_acb),
            VietnamBank("agribank", "Agribank", "AGR", R.drawable.bank_logo_agribank),
            VietnamBank("anz_bank", "ANZ Bank", "ANZ", R.drawable.bank_logo_anz_bank),
            VietnamBank("bacabank", "Bac A Bank", "BAB", R.drawable.bank_logo_bacabank),
            VietnamBank("baovietbank", "BaoVietBank", "BVB", R.drawable.bank_logo_baovietbank),
            VietnamBank("bidv", "BIDV", "BIDV", R.drawable.bank_logo_bidv),
            VietnamBank("bvbank", "BVBank", "BVB", R.drawable.bank_logo_bvbank),
            VietnamBank("cbbank", "CBBank", "CBB", R.drawable.bank_logo_cbbank),
            VietnamBank("co_opbank", "Co-opBank", "COOP", R.drawable.bank_logo_co_opbank),
            VietnamBank("dongabank", "DongA Bank", "DAB", R.drawable.bank_logo_dongabank),
            VietnamBank("eximbank", "Eximbank", "EIB", R.drawable.bank_logo_eximbank),
            VietnamBank("gpbank", "GPBank", "GPB", R.drawable.bank_logo_gpbank),
            VietnamBank("hdbank", "HDBank", "HDB", R.drawable.bank_logo_hdbank),
            VietnamBank("hong_leong_bank", "Hong Leong Bank", "HLB", R.drawable.bank_logo_hong_leong_bank),
            VietnamBank("hsbc", "HSBC", "HSBC", R.drawable.bank_logo_hsbc),
            VietnamBank("indovina", "Indovina Bank", "IVB", R.drawable.bank_logo_indovina),
            VietnamBank("kienlongbank", "KienlongBank", "KLB", R.drawable.bank_logo_kienlongbank),
            VietnamBank("lpbank", "LPBank", "LPB", R.drawable.bank_logo_lpbank),
            VietnamBank("mbbank", "MB Bank", "MB", R.drawable.bank_logo_mbbank),
            VietnamBank("msb", "MSB", "MSB", R.drawable.bank_logo_msb),
            VietnamBank("namabank", "Nam A Bank", "NAB", R.drawable.bank_logo_namabank),
            VietnamBank("ncb", "NCB", "NCB", R.drawable.bank_logo_ncb),
            VietnamBank("ocb", "OCB", "OCB", R.drawable.bank_logo_ocb),
            VietnamBank("oceanbank", "OceanBank", "OCEAN", R.drawable.bank_logo_oceanbank),
            VietnamBank("public_bank", "Public Bank", "PBB", R.drawable.bank_logo_public_bank),
            VietnamBank("pvcombank", "PVcomBank", "PVCB", R.drawable.bank_logo_pvcombank),
            VietnamBank("sacombank", "Sacombank", "STB", R.drawable.bank_logo_sacombank),
            VietnamBank("saigonbank", "Saigonbank", "SGB", R.drawable.bank_logo_saigonbank),
            VietnamBank("scb", "SCB", "SCB", R.drawable.bank_logo_scb),
            VietnamBank("seabank", "SeABank", "SEA", R.drawable.bank_logo_seabank),
            VietnamBank("shb", "SHB", "SHB", R.drawable.bank_logo_shb),
            VietnamBank("shinhan_bank", "Shinhan Bank", "SHIN", R.drawable.bank_logo_shinhan_bank),
            VietnamBank("standard_chartered", "Standard Chartered", "SC", R.drawable.bank_logo_standard_chartered),
            VietnamBank("techcombank", "Techcombank", "TCB", R.drawable.bank_logo_techcombank),
            VietnamBank("tpbank", "TPBank", "TPB", R.drawable.bank_logo_tpbank),
            VietnamBank("uob", "UOB", "UOB", R.drawable.bank_logo_uob),
            VietnamBank("vdb", "VDB", "VDB", R.drawable.bank_logo_vdb),
            VietnamBank("vib", "VIB", "VIB", R.drawable.bank_logo_vib),
            VietnamBank("vietabank", "VietABank", "VAB", R.drawable.bank_logo_vietabank),
            VietnamBank("vietbank", "Vietbank", "VB", R.drawable.bank_logo_vietbank),
            VietnamBank("vietcombank", "Vietcombank", "VCB", R.drawable.bank_logo_vietcombank_1),
            VietnamBank("vietinbank", "VietinBank", "CTG", R.drawable.bank_logo_vietinbank),
            VietnamBank("vpbank", "VPBank", "VPB", R.drawable.bank_logo_vpbank),
            VietnamBank("woori_bank", "Woori Bank", "WORI", R.drawable.bank_logo_woori_bank),
        )

        /**
         * Tìm ngân hàng theo ID.
         * @param id ID ngân hàng (vd: "vietcombank", "techcombank")
         * @return [VietnamBank] hoặc null nếu không tìm thấy
         */
        fun findBankById(id: String): VietnamBank? =
            com.example.mobileproject.presentation.model.wallet.VietnamBankCatalog.findById(id)
    }
}
