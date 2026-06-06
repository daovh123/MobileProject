package com.example.mobileproject.domain.entity

/**
 * Domain entity đại diện cho một giao dịch tài chính trong ví chung của cặp đôi.
 * Mỗi giao dịch ghi lại một lần thu hoặc chi, phục vụ quản lý tài chính và phân tích.
 *
 * @property id Định danh duy nhất của giao dịch
 * @property coupleId Định danh cặp đôi sở hữu giao dịch này
 * @property amount Số tiền giao dịch (đơn vị: VNĐ), luôn dương
 * @property type Loại giao dịch: INCOME (thu) hoặc EXPENSE (chi)
 * @property category Danh mục chi tiêu/thu nhập (ví dụ: "Ăn uống", "Lương")
 * @property note Ghi chú mô tả giao dịch do người dùng nhập
 * @property createdAt Thời điểm tạo giao dịch dạng chuỗi ISO
 */
data class Transaction(
    val id: String,
    val coupleId: String,
    val amount: Long,
    val type: TransactionType,
    val category: String,
    val note: String,
    val createdAt: String
)

/**
 * Enum phân loại loại giao dịch.
 *
 * - INCOME: Thu nhập — tiền được thêm vào ví (lương, thưởng, hoàn tiền)
 * - EXPENSE: Chi tiêu — tiền được trừ khỏi ví (mua sắm, ăn uống, giải trí)
 */
enum class TransactionType {
    INCOME,
    EXPENSE
}

/**
 * Domain entity chứa kết quả trả về từ API khi tạo hoặc truy vấn giao dịch.
 * Các trường nullable phản ánh việc server có thể không trả về đầy đủ thông tin
 * trong mọi trường hợp (ví dụ: khi tạo thành công chỉ trả về id và số dư mới).
 *
 * @property success True nếu thao tác thành công
 * @property message Thông điệp phản hồi từ server
 * @property transactionId Định danh giao dịch được tạo, null nếu thao tác thất bại
 * @property amount Số tiền của giao dịch
 * @property type Loại giao dịch dưới dạng chuỗi (cần parse thành [TransactionType])
 * @property category Danh mục giao dịch
 * @property note Ghi chú giao dịch
 * @property totalBalance Số dư ví sau giao dịch, dùng để cập nhật UI real-time
 * @property createdAt Thời điểm tạo giao dịch
 */
data class TransactionResponse(
    val success: Boolean,
    val message: String,
    val transactionId: String?,
    val amount: Long?,
    val type: String?,
    val category: String?,
    val note: String?,
    val totalBalance: Long?,
    val createdAt: String?
)
