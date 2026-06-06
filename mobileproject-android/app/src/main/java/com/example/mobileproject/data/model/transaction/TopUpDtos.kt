package com.example.mobileproject.data.model.transaction

import com.google.gson.annotations.SerializedName

/**
 * Request body cho API tạo yêu cầu nạp tiền (top-up) vào ví.
 *
 * Hệ thống sẽ tạo một giao dịch top-up với thông tin ngân hàng,
 * trả về QR code để người dùng quét và chuyển khoản.
 */
data class TopUpCreateRequestDto(
    /** UUID của couple, bắt buộc. */
    @SerializedName("coupleId") val coupleId: String,
    /** Số tiền nạp (VNĐ), bắt buộc, phải > 0. */
    @SerializedName("amount") val amount: Long,
    /** Mã ngân hàng (VD: "VCB", "TCB"), bắt buộc. */
    @SerializedName("bankId") val bankId: String,
    /** Tên hiển thị ngân hàng (VD: "Vietcombank"), bắt buộc. */
    @SerializedName("bankName") val bankName: String,
    /** Ghi chú cho giao dịch nạp tiền, nullable. */
    @SerializedName("note") val note: String?,
)

/**
 * Response từ API tạo yêu cầu nạp tiền.
 *
 * Chứa thông tin tài khoản nhận tiền, QR code, và mã chuyển khoản.
 * [status] cho biết trạng thái giao dịch: "PENDING", "PAID", "EXPIRED", v.v.
 *
 * Các trường nullable vì một số thông tin chỉ có sau khi tạo giao dịch thành công.
 */
data class TopUpResponseDto(
    /** UUID của giao dịch nạp tiền, nullable. */
    @SerializedName("id") val id: String?,
    /** UUID của couple, nullable. */
    @SerializedName("coupleId") val coupleId: String?,
    /** Số tiền nạp (VNĐ), nullable. */
    @SerializedName("amount") val amount: Long?,
    /** Trạng thái giao dịch ("PENDING", "PAID", "EXPIRED"), nullable. */
    @SerializedName("status") val status: String?,
    /** Mã ngân hàng nhận tiền, nullable. */
    @SerializedName("bankId") val bankId: String?,
    /** Tên ngân hàng nhận tiền, nullable. */
    @SerializedName("bankName") val bankName: String?,
    /** Số tài khoản nhận tiền, nullable. */
    @SerializedName("accountNumber") val accountNumber: String?,
    /** Tên chủ tài khoản nhận tiền, nullable. */
    @SerializedName("accountName") val accountName: String?,
    /** Mã chuyển khoản duy nhất, nullable. */
    @SerializedName("transferCode") val transferCode: String?,
    /** Nội dung chuyển khoản cần ghi, nullable. */
    @SerializedName("transferContent") val transferContent: String?,
    /** Dữ liệu QR code dạng chuỗi, nullable. */
    @SerializedName("qrContent") val qrContent: String?,
    /** URL hình ảnh QR code, nullable. */
    @SerializedName("qrImageUrl") val qrImageUrl: String?,
    /** Ghi chú, nullable. */
    @SerializedName("note") val note: String?,
    /** Số dư ví hiện tại (VNĐ), nullable. */
    @SerializedName("currentBalance") val currentBalance: Long?,
    /** Thời điểm tạo giao dịch, định dạng ISO datetime, nullable. */
    @SerializedName("createdAt") val createdAt: String?,
    /** Thời điểm thanh toán thành công, định dạng ISO datetime, nullable.
     * Chỉ có giá trị khi [status] = "PAID". */
    @SerializedName("paidAt") val paidAt: String?,
)

/**
 * Request body cho API tạo yêu cầu rút tiền (payout) từ ví.
 *
 * Backend sẽ xử lý chuyển tiền về tài khoản ngân hàng đã đăng ký.
 */
data class PayoutCreateRequestDto(
    /** UUID của couple, bắt buộc. */
    @SerializedName("coupleId") val coupleId: String,
    /** Số tiền rút (VNĐ), bắt buộc, phải > 0 và không vượt quá số dư ví. */
    @SerializedName("amount") val amount: Long,
)

/**
 * Response từ API tạo yêu cầu rút tiền.
 *
 * [status] cho biết trạng thái: "PENDING", "PAID", "FAILED", v.v.
 */
data class PayoutResponseDto(
    /** UUID của giao dịch rút tiền, nullable. */
    @SerializedName("id") val id: String?,
    /** UUID của couple, nullable. */
    @SerializedName("coupleId") val coupleId: String?,
    /** Số tiền rút (VNĐ), nullable. */
    @SerializedName("amount") val amount: Long?,
    /** Trạng thái giao dịch ("PENDING", "PAID", "FAILED"), nullable. */
    @SerializedName("status") val status: String?,
    /** Mã chuyển khoản, nullable. */
    @SerializedName("transferCode") val transferCode: String?,
    /** Số dư ví hiện tại (VNĐ), nullable. */
    @SerializedName("currentBalance") val currentBalance: Long?,
    /** Thời điểm tạo giao dịch, định dạng ISO datetime, nullable. */
    @SerializedName("createdAt") val createdAt: String?,
    /** Thời điểm rút tiền thành công, định dạng ISO datetime, nullable.
     * Chỉ có giá trị khi [status] = "PAID". */
    @SerializedName("paidAt") val paidAt: String?,
)
