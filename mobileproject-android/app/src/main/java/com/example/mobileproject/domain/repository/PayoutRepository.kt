package com.example.mobileproject.domain.repository

import com.example.mobileproject.core.result.Resource
import com.example.mobileproject.domain.entity.PayoutRequest

/**
 * Repository quản lý yêu cầu rút tiền (payout) từ ví chung của cặp đôi.
 *
 * Quy trình rút tiền: người dùng nhập số tiền → tạo yêu cầu payout →
 * hệ thống xử lý chuyển tiền → cập nhật số dư ví.
 */
interface PayoutRepository {

    /**
     * Tạo yêu cầu rút tiền mới từ ví chung.
     *
     * @param coupleId ID duy nhất của cặp đôi
     * @param amount Số tiền rút (đơn vị: VND), phải lớn hơn 0 và không vượt quá số dư ví
     * @return [Resource.Success] chứa [PayoutRequest] với thông tin yêu cầu rút tiền,
     *         [Resource.Error] khi số dư không đủ hoặc số tiền không hợp lệ
     */
    suspend fun createPayout(coupleId: String, amount: Long): Resource<PayoutRequest>

    /**
     * Lấy thông tin chi tiết của một yêu cầu rút tiền theo ID.
     *
     * Dùng để kiểm tra trạng thái xử lý (pending, completed, failed)
     * sau khi tạo yêu cầu rút tiền.
     *
     * @param id ID của yêu cầu rút tiền
     * @return [Resource.Success] chứa [PayoutRequest] với trạng thái hiện tại,
     *         [Resource.Error] khi yêu cầu không tồn tại
     */
    suspend fun getPayout(id: String): Resource<PayoutRequest>
}

