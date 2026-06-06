package com.example.mobileproject.domain.repository

import com.example.mobileproject.core.result.Resource
import com.example.mobileproject.domain.entity.Transaction
import com.example.mobileproject.domain.entity.TransactionResponse

/**
 * Repository quản lý giao dịch tài chính (transactions) của cặp đôi.
 *
 * Bao gồm tạo giao dịch chi tiêu, xử lý thu nhập, và truy vấn lịch sử giao dịch.
 * Mỗi giao dịch được phân loại theo loại (thu/chi) và danh mục chi tiêu.
 */
interface TransactionRepository {

    /**
     * Tạo giao dịch chi tiêu mới cho cặp đôi.
     *
     * Giao dịch sẽ ảnh hưởng đến số dư ví chung và được phân tích
     * trong các báo cáo thống kê theo danh mục.
     *
     * @param coupleId ID duy nhất của cặp đôi
     * @param amount Số tiền giao dịch (đơn vị: VND), phải lớn hơn 0
     * @param type Loại giao dịch: "expense" (chi tiêu) hoặc "income" (thu nhập)
     * @param category Mã danh mục chi tiêu (xem [com.example.mobileproject.domain.model.ExpenseCategory])
     * @param note Ghi chú mô tả giao dịch, null nếu không có
     * @return [Resource.Success] chứa [TransactionResponse] với thông tin giao dịch đã tạo,
     *         [Resource.Error] khi validation thất bại hoặc số dư không đủ
     */
    suspend fun createTransaction(
        coupleId: String,
        amount: Long,
        type: String,
        category: String,
        note: String?
    ): Resource<TransactionResponse>

    /**
     * Xử lý thu nhập — phân bổ tiền vào ví chung hoặc trực tiếp vào mục tiêu.
     *
     * Khác với [createTransaction] ở loại "income", phương thức này
     * cho phép chỉ định mục tiêu nhận tiền cụ thể, giúp tự động hóa
     * quy trình phân bổ thu nhập cho cặp đôi.
     *
     * @param coupleId ID duy nhất của cặp đôi
     * @param amount Số tiền thu nhập (đơn vị: VND), phải lớn hơn 0
     * @param targetType Nơi nhận tiền: "wallet" (ví chung) hoặc "goal" (mục tiêu)
     * @param goalId ID mục tiêu nhận tiền, chỉ bắt buộc khi [targetType] là "goal"
     * @param note Ghi chú mô tả, null nếu không có
     * @return [Resource.Success] chứa [TransactionResponse],
     *         [Resource.Error] khi targetType là "goal" mà goalId null hoặc không tồn tại
     */
    suspend fun processIncome(
        coupleId: String,
        amount: Long,
        targetType: String,
        goalId: String?,
        note: String?
    ): Resource<TransactionResponse>

    /**
     * Lấy lịch sử tất cả giao dịch của cặp đôi.
     *
     * @param coupleId ID duy nhất của cặp đôi
     * @return [Resource.Success] chứa danh sách [Transaction] sắp xếp theo thời gian giảm dần,
     *         [Resource.Error] khi có lỗi truy vấn
     */
    suspend fun getTransactions(coupleId: String): Resource<List<Transaction>>
}
