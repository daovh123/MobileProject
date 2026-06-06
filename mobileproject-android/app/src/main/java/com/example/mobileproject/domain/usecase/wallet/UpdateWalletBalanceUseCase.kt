package com.example.mobileproject.domain.usecase.wallet

import com.example.mobileproject.domain.repository.WalletRepository
import javax.inject.Inject

/**
 * Use case cập nhật số dư ví trong local cache.
 *
 * Thực hiện optimistic update — cập nhật UI ngay lập tức sau giao dịch
 * mà không cần gọi lại API. Hữu ích khi:
 * - Vừa tạo giao dịch chi tiêu → trừ tiền ngay trên UI
 * - Vừa nhận top-up → cộng tiền ngay trên UI
 * - Server trả về số dư mới → đồng bộ local
 */
class UpdateWalletBalanceUseCase @Inject constructor(
    private val repository: WalletRepository
) {
    suspend operator fun invoke(newBalance: Long) {
        repository.updateLocalBalance(newBalance)
    }
}