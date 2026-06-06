package com.example.mobileproject.data.repository

import com.example.mobileproject.core.result.Resource
import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.data.datasource.remote.ApiService
import com.example.mobileproject.data.mapper.toDomain
import com.example.mobileproject.data.model.transaction.PayoutCreateRequestDto
import com.example.mobileproject.domain.entity.PayoutRequest
import com.example.mobileproject.domain.repository.PayoutRepository
import javax.inject.Inject

/**
 * Implementation của [PayoutRepository], xử lý yêu cầu rút tiền từ ví.
 *
 * ## Caching strategy
 * Không có cache, mỗi lần gọi fetch từ API.
 * Dữ liệu giao dịch tài chính cần realtime accuracy.
 *
 * ## Data transformation
 * Chuyển đổi [PayoutResponseDto] -> [PayoutRequest] (domain entity) qua mapper `toDomain()`.
 *
 * ## Error handling
 * Trả về [Resource.Error] khi API call thất bại, bao gồm cả error body message.
 *
 * ## Threading
 * Tất cả hàm `suspend` chạy trên IO dispatcher (Retrofit default).
 */
class PayoutRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val authSessionStore: AuthSessionStore,
) : PayoutRepository {

    /**
     * Tạo header xác thực từ token đã lưu.
     *
     * @return "Bearer <token>"
     * @throws IllegalStateException nếu token rỗng
     */
    private fun authorizationHeader(): String {
        val token = authSessionStore.load()?.token?.trim().orEmpty()
        if (token.isBlank()) throw IllegalStateException("Missing auth token")
        return "Bearer $token"
    }

    /**
     * Tạo yêu cầu rút tiền từ ví chung.
     *
     * @param coupleId ID cặp đôi
     * @param amount số tiền rút (VND)
     * @return [Resource]<[PayoutRequest]> thông tin yêu cầu rút tiền
     */
    override suspend fun createPayout(coupleId: String, amount: Long): Resource<PayoutRequest> {
        return try {
            val response = apiService.createPayout(
                authorizationHeader(),
                PayoutCreateRequestDto(coupleId = coupleId, amount = amount),
            )
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.toDomain())
            } else {
                Resource.Error(Exception(response.errorBody()?.string() ?: response.message()))
            }
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    /**
     * Lấy thông tin chi tiết một yêu cầu rút tiền.
     *
     * Dùng để kiểm tra trạng thái yêu cầu (polling) sau khi tạo.
     *
     * @param id ID yêu cầu rút tiền
     * @return [Resource]<[PayoutRequest]>
     */
    override suspend fun getPayout(id: String): Resource<PayoutRequest> {
        return try {
            val response = apiService.getPayout(authorizationHeader(), id)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.toDomain())
            } else {
                Resource.Error(Exception(response.errorBody()?.string() ?: response.message()))
            }
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }
}

