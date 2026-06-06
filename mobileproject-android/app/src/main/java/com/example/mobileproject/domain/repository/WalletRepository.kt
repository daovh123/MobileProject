package com.example.mobileproject.domain.repository

import com.example.mobileproject.domain.entity.Wallet
import kotlinx.coroutines.flow.Flow

/**
 * Repository quản lý ví chung (shared wallet) của cặp đôi.
 *
 * Ví chung là nơi cặp đôi quản lý tài chính chung: theo dõi số dư,
 * nạp/rút tiền, và xem lịch sử giao dịch.
 */
interface WalletRepository {

    /**
     * Lấy thông tin ví chung của cặp đôi từ server.
     *
     * @param coupleId ID duy nhất của cặp đôi
     * @param token Token xác thực để truy cập API
     * @return Flow phát ra [Result.success] chứa [Wallet],
     *         [Result.failure] khi có lỗi mạng hoặc xác thực
     */
    fun getWallet(coupleId: String, token: String): Flow<Result<Wallet>>

    /**
     * Cập nhật số dư ví trong local cache.
     *
     * Dùng để cập nhật UI ngay lập tức sau khi có giao dịch
     * mà không cần gọi lại API (optimistic update).
     *
     * @param newBalance Số dư mới (đơn vị: VND)
     */
    suspend fun updateLocalBalance(newBalance: Long)

    /**
     * Luồng phát ra trạng thái ví hiện tại từ local cache.
     *
     * Giá trị null nghĩa là ví chưa được tải lần đầu.
     * UI nên observe flow này để hiển thị số dư real-time.
     */
    val walletState: Flow<Wallet?>
}