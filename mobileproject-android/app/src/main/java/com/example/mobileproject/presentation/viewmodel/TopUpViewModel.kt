package com.example.mobileproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileproject.R
import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.domain.repository.TransactionRepository
import com.example.mobileproject.domain.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VietnamBank(
    val id: String,
    val name: String,
    val shortName: String,
    val logoRes: Int,
)

enum class TopUpStep {
    AMOUNT,
    BANK_SELECT,
}

data class TopUpUiState(
    val amount: String = "",
    val note: String = "",
    val destination: String = "Nap vao Vi chinh",
    val currentBalance: Long = 0L,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val currentStep: TopUpStep = TopUpStep.AMOUNT,
    val selectedBank: VietnamBank? = null,
) {
    val predictedBalance: Long
        get() {
            val addAmount = amount.toLongOrNull() ?: 0L
            return currentBalance + addAmount
        }

    val isAmountValid: Boolean
        get() = (amount.toLongOrNull() ?: 0L) > 0
}

@HiltViewModel
class TopUpViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository,
    private val authSessionStore: AuthSessionStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TopUpUiState())
    val uiState: StateFlow<TopUpUiState> = _uiState.asStateFlow()

    init {
        loadCurrentBalance()
    }

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

    fun onAmountChange(newAmount: String) {
        if (newAmount.all { it.isDigit() }) {
            _uiState.update { it.copy(amount = newAmount) }
        }
    }

    fun onNoteChange(newNote: String) {
        _uiState.update { it.copy(note = newNote) }
    }

    fun onBankSelected(bank: VietnamBank) {
        _uiState.update { it.copy(selectedBank = bank) }
    }

    fun goToNextStep() {
        val state = _uiState.value
        if (state.currentStep == TopUpStep.AMOUNT && state.isAmountValid) {
            _uiState.update { it.copy(currentStep = TopUpStep.BANK_SELECT) }
        }
    }

    fun goToPreviousStep() {
        val state = _uiState.value
        if (state.currentStep == TopUpStep.BANK_SELECT) {
            _uiState.update { it.copy(currentStep = TopUpStep.AMOUNT, selectedBank = null) }
        }
    }

    fun topUpNow() {
        val state = _uiState.value
        val amountLong = state.amount.toLongOrNull() ?: 0L
        if (amountLong <= 0) {
            _uiState.update { it.copy(error = "Please enter a valid amount") }
            return
        }

        val session = authSessionStore.load()
        val coupleId = session?.coupleId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = transactionRepository.createTransaction(
                coupleId = coupleId,
                amount = amountLong,
                type = "INCOME",
                category = "INCOME",
                note = state.note,
            )

            when (result) {
                is com.example.mobileproject.core.result.Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                }

                is com.example.mobileproject.core.result.Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.throwable.message ?: "Top up failed",
                        )
                    }
                }

                else -> _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    companion object {
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

        fun findBankById(id: String): VietnamBank? {
            return vietnamBanks.firstOrNull { it.id == id }
        }
    }
}
