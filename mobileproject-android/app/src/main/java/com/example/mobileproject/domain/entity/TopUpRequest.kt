package com.example.mobileproject.domain.entity

/**
 * Enum đại diện cho trạng thái của yêu cầu nạp tiền (top-up).
 *
 * - PENDING: Đang chờ người dùng chuyển khoản, hiển thị mã QR và thông tin chuyển khoản
 * - PAID: Đã nhận được tiền, ví đã được cộng số dư
 * - FAILED: Giao dịch thất bại (sai số tiền, hết hạn xác nhận từ cổng thanh toán)
 * - EXPIRED: Yêu cầu nạp tiền đã hết hạn, người dùng cần tạo yêu cầu mới
 * - UNKNOWN: Trạng thái không xác định, thường do lỗi đồng bộ từ cổng thanh toán
 */
enum class TopUpStatus {
    PENDING,
    PAID,
    FAILED,
    EXPIRED,
    UNKNOWN,
}

/**
 * Domain entity đại diện cho yêu cầu nạp tiền vào ví couple.
 * Chứa đầy đủ thông tin chuyển khoản ngân hàng và mã QR để người dùng thực hiện nạp tiền.
 *
 * @property id Định danh duy nhất của yêu cầu nạp tiền
 * @property coupleId Định danh cặp đôi thực hiện nạp tiền
 * @property amount Số tiền cần nạp (đơn vị: VNĐ)
 * @property status Trạng thái hiện tại của yêu cầu nạp tiền
 * @property bankId Định danh ngân hàng nhận tiền
 * @property bankName Tên ngân hàng nhận tiền (ví dụ: "Vietcombank"), hiển thị cho người dùng
 * @property accountNumber Số tài khoản nhận tiền
 * @property accountName Tên chủ tài khoản nhận tiền
 * @property transferCode Mã chuyển khoản duy nhất, người dùng phải nhập đúng mã này để hệ thống xác nhận
 * @property transferContent Nội dung chuyển khoản, cần được điền chính xác khi chuyển tiền
 * @property qrContent Dữ liệu mã QR dạng chuỗi, dùng để tạo QR code trong app
 * @property qrImageUrl URL hình ảnh mã QR đã được render sẵn từ server, null nếu cần tự render từ qrContent
 * @property createdAt Thời điểm tạo yêu cầu nạp tiền
 * @property paidAt Thời điểm xác nhận đã thanh toán, null nếu chưa thanh toán
 * @property currentBalance Số dư ví sau khi nạp thành công, null nếu chưa thanh toán
 */
data class TopUpRequest(
    val id: String,
    val coupleId: String,
    val amount: Long,
    val status: TopUpStatus,
    val bankId: String,
    val bankName: String,
    val accountNumber: String,
    val accountName: String,
    val transferCode: String,
    val transferContent: String,
    val qrContent: String,
    val qrImageUrl: String?,
    val createdAt: String,
    val paidAt: String?,
    val currentBalance: Long?,
)
