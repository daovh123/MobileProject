package com.example.mobileproject.domain.repository

import com.example.mobileproject.core.result.Resource
import com.example.mobileproject.domain.entity.TopUpRequest

/**
 * Repository quản lý yêu cầu nạp tiền (top-up) vào ví chung của cặp đôi.
 *
 * Quy trình nạp tiền: người dùng chọn ngân hàng, nhập số tiền → tạo yêu cầu top-up →
 * hệ thống xử lý thanh toán → cập nhật số dư ví.
 */
interface TopUpRepository {

    /**
     * Tạo yêu cầu nạp tiền mới vào ví chung.
     *
     * @param coupleId ID duy nhất của cặp đôi
     * @param amount Số tiền nạp (đơn vị: VND), phải lớn hơn 0
     * @param bankId Mã ngân hàng (ví dụ: "VCB", "TCB", "MB")
     * @param bankName Tên ngân hàng hiển thị (ví dụ: "Vietcombank", "Techcombank")
     * @param note Ghi chú cho giao dịch, null nếu không có
     * @return [Resource.Success] chứa [TopUpRequest] với thông tin yêu cầu nạp tiền,
     *         [Resource.Error] khi số tiền không hợp lệ hoặc ngân hàng không hỗ trợ
     */
    suspend fun createTopUp(
        coupleId: String,
        amount: Long,
        bankId: String,
        bankName: String,
        note: String?,
    ): Resource<TopUpRequest>

    /**
     * Lấy thông tin chi tiết của một yêu cầu nạp tiền theo ID.
     *
     * Dùng để kiểm tra trạng thái xử lý (pending, completed, failed)
     * sau khi tạo yêu cầu nạp tiền.
     *
     * @param id ID của yêu cầu nạp tiền
     * @return [Resource.Success] chứa [TopUpRequest] với trạng thái hiện tại,
     *         [Resource.Error] khi yêu cầu không tồn tại
     */
    suspend fun getTopUp(id: String): Resource<TopUpRequest>
}
