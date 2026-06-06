package com.example.mobileproject.data.repository

import com.example.mobileproject.core.result.Resource
import com.example.mobileproject.data.datasource.local.AuthSessionStore
import com.example.mobileproject.data.datasource.remote.ApiService
import com.example.mobileproject.data.mapper.toDomain
import com.example.mobileproject.data.model.transaction.TopUpCreateRequestDto
import com.example.mobileproject.domain.entity.TopUpRequest
import com.example.mobileproject.domain.repository.TopUpRepository
import javax.inject.Inject

/**
 * Implementation của [TopUpRepository], xử lý yêu cầu nạp tiền vào ví.
 *
 * ## Caching strategy
 * Không có cache, mỗi lần gọi fetch từ API.
 * Dữ liệu giao dịch tài chính cần realtime accuracy.
 *
 * ## Data transformation
 * Chuyển đổi [TopUpResponseDto] -> [TopUpRequest] (domain entity) qua mapper `toDomain()`.
 *
 * ## Error handling
 * Trả về [Resource.Error] khi API call thất bại, bao gồm cả error body message.
 *
 * ## Threading
 * Tất cả hàm `suspend` chạy trên IO dispatcher (Retrofit default).
 */
class TopUpRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val authSessionStore: AuthSessionStore,
) : TopUpRepository {

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
     * Tạo yêu cầu nạp tiền vào ví.
     *
     * @param coupleId ID cặp đôi
     * @param amount số tiền nạp (VND)
     * @param bankId mã ngân hàng
     * @param bankName tên ngân hàng
     * @param note ghi chú (nullable)
     * @return [Resource]<[TopUpRequest]> thông tin yêu cầu nạp tiền
     */
    override suspend fun createTopUp(
        coupleId: String,
        amount: Long,
        bankId: String,
        bankName: String,
        note: String?,
    ): Resource<TopUpRequest> {
        return try {
            val response = apiService.createTopUp(
                authorizationHeader(),
                TopUpCreateRequestDto(
                    coupleId = coupleId,
                    amount = amount,
                    bankId = bankId,
                    bankName = bankName,
                    note = note,
                ),
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
     * Lấy thông tin chi tiết một yêu cầu nạp tiền.
     *
     * Dùng để kiểm tra trạng thái yêu cầu (polling) sau khi tạo.
     *
     * @param id ID yêu cầu nạp tiền
     * @return [Resource]<[TopUpRequest]>
     */
    override suspend fun getTopUp(id: String): Resource<TopUpRequest> {
        return try {
            val response = apiService.getTopUp(authorizationHeader(), id)
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
