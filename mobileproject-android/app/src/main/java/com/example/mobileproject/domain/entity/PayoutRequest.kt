package com.example.mobileproject.domain.entity

/**
 * Enum đại diện cho trạng thái của yêu cầu rút tiền (payout).
 *
 * - PENDING: Đang chờ hệ thống xử lý chuyển tiền ra tài khoản ngân hàng
 * - PAID: Đã chuyển tiền thành công, số dư ví đã bị trừ
 * - FAILED: Rút tiền thất bại (sai thông tin tài khoản, lỗi hệ thống ngân hàng)
 * - UNKNOWN: Trạng thái không xác định
 */
enum class PayoutStatus {
    PENDING,
    PAID,
    FAILED,
    UNKNOWN,
}

/**
 * Domain entity đại diện cho yêu cầu rút tiền từ ví couple ra tài khoản ngân hàng.
 * Íp hơn [TopUpRequest] vì không cần thông tin QR — server tự xử lý chuyển khoản.
 *
 * @property id Định danh duy nhất của yêu cầu rút tiền
 * @property coupleId Định danh cặp đôi thực hiện rút tiền
 * @property amount Số tiền muốn rút (đơn vị: VNĐ)
 * @property status Trạng thái hiện tại của yêu cầu rút tiền
 * @property transferCode Mã giao dịch, dùng để truy vết và đối chiếu với hệ thống ngân hàng
 * @property createdAt Thời điểm tạo yêu cầu rút tiền
 * @property paidAt Thời điểm xác nhận đã chuyển tiền thành công, null nếu chưa xử lý
 * @property currentBalance Số dư ví còn lại sau khi rút thành công, null nếu chưa xử lý
 */
data class PayoutRequest(
    val id: String,
    val coupleId: String,
    val amount: Long,
    val status: PayoutStatus,
    val transferCode: String,
    val createdAt: String,
    val paidAt: String?,
    val currentBalance: Long?,
)
