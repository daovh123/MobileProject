package com.example.mobileproject.data.repository

import android.util.Log
import com.example.mobileproject.data.datasource.remote.ApiService
import com.example.mobileproject.data.model.remote.WalletResponse
import com.example.mobileproject.domain.entity.Wallet
import com.example.mobileproject.domain.repository.WalletRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation của [WalletRepository], quản lý ví chung của cặp đôi.
 *
 * ## Caching strategy
 * - Sử dụng [MutableStateFlow] để giữ trạng thái ví trong bộ nhớ
 * - [walletState] phát giá trị hiện tại cho UI (in-memory cache)
 * - Không có persistent cache, mỗi lần app khởi động lại cần fetch từ API
 *
 * ## Error handling
 * - Trả về [Result.failure] với message lỗi chi tiết
 * - Xử lý cả HTTP errors và network exceptions
 * - Log lỗi với tag "WalletRepo"
 *
 * ## Threading
 * - [getWallet] là Flow chạy trên IO dispatcher
 * - [updateLocalBalance] cập nhật StateFlow (thread-safe do MutableStateFlow)
 */
@Singleton
class WalletRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val gson: Gson
) : WalletRepository {

    /** Trạng thái ví hiện tại trong bộ nhớ, null nếu chưa fetch. */
    private val _walletState = MutableStateFlow<Wallet?>(null)
    override val walletState: Flow<Wallet?> = _walletState.asStateFlow()

    /**
     * Lấy thông tin ví chung từ API.
     *
     * Cập nhật [_walletState] sau khi fetch thành công.
     * Map `WalletResponse` -> `Wallet` domain entity, với giá trị mặc định:
     * - name = "Ví chung"
     * - balance = 0
     *
     * @param coupleId ID cặp đôi
     * @param token JWT token (không có prefix "Bearer")
     * @return Flow<Result<Wallet>>
     */
    override fun getWallet(coupleId: String, token: String): Flow<Result<Wallet>> = flow {
        try {
            Log.d("WalletRepo", "Request URL ID: $coupleId")
            val response = apiService.getWallet("Bearer $token", coupleId)
            
            if (response.isSuccessful) {
                val body = response.body()
                Log.d("WalletRepo", "Full Response Body: $body")
                
                if (body != null) {
                    val wallet = Wallet(
                        idCouple = body.idCouple ?: coupleId,
                        name = body.walletName ?: "Ví chung",
                        balance = body.totalBalance ?: 0L
                    )
                    _walletState.value = wallet
                    emit(Result.success(wallet))
                } else {
                    emit(Result.failure(Exception("Response body is null")))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "HTTP ${response.code()}: ${errorBody ?: response.message()}"
                Log.e("WalletRepo", errorMsg)
                emit(Result.failure(Exception(errorMsg)))
            }
        } catch (e: Exception) {
            Log.e("WalletRepo", "Exception during call", e)
            emit(Result.failure(Exception("Network error: ${e.localizedMessage}")))
        }
    }

    /**
     * Cập nhật số dư ví trong bộ nhớ (optimistic update).
     *
     * Dùng sau khi tạo giao dịch thành công để UI cập nhật ngay mà không cần fetch lại.
     * Nếu chưa có [_walletState], tạo mới với coupleId rỗng.
     *
     * @param newBalance số dư mới (VND)
     */
    override suspend fun updateLocalBalance(newBalance: Long) {
        val currentState = _walletState.value
        if (currentState != null) {
            _walletState.value = currentState.copy(balance = newBalance)
        } else {
            _walletState.value = Wallet(idCouple = "", name = "Ví chung", balance = newBalance)
        }
    }
}