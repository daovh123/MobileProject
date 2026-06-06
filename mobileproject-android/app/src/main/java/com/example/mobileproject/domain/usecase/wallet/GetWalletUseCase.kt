package com.example.mobileproject.domain.usecase.wallet

import com.example.mobileproject.domain.entity.Wallet
import com.example.mobileproject.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case lấy thông tin ví chung của cặp đôi.
 *
 * Cung cấp entry point cho ViewModel observe trạng thái ví (số dư, lịch sử)
 * thông qua Flow, đảm bảo UI tự động cập nhật khi dữ liệu thay đổi.
 */
class GetWalletUseCase @Inject constructor(
    private val repository: WalletRepository
) {
    operator fun invoke(coupleId: String, token: String): Flow<Result<Wallet>> {
        return repository.getWallet(coupleId, token)
    }
}