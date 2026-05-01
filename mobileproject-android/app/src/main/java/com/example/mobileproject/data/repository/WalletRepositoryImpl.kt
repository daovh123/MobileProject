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

@Singleton
class WalletRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val gson: Gson
) : WalletRepository {

    private val _walletState = MutableStateFlow<Wallet?>(null)
    override val walletState: Flow<Wallet?> = _walletState.asStateFlow()

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

    override suspend fun updateLocalBalance(newBalance: Long) {
        val currentState = _walletState.value
        if (currentState != null) {
            _walletState.value = currentState.copy(balance = newBalance)
        } else {
            _walletState.value = Wallet(idCouple = "", name = "Ví chung", balance = newBalance)
        }
    }
}