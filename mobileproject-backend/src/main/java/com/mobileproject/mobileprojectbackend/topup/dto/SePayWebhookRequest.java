package com.mobileproject.mobileprojectbackend.topup.dto;

/**
 * DTO request đại diện cho payload webhook từ SePay.
 *
 * <p>SePay gửi webhook khi có giao dịch chuyển khoản đến/đi.
 * Các trường khớp với cấu trúc JSON của SePay API.</p>
 *
 * @param id             ID giao dịch SePay (bắt buộc để xử lý)
 * @param gateway        cổng thanh toán (ví dụ: "MBBank")
 * @param transactionDate thời điểm giao dịch
 * @param accountNumber  số tài khoản nhận/gửi
 * @param subAccount     tài khoản phụ (dùng cho BIDV/VA)
 * @param code           mã giao dịch (có thể là transferCode)
 * @param content        nội dung chuyển khoản
 * @param transferType   loại chuyển: "in" (đến) hoặc "out" (đi)
 * @param description    mô tả giao dịch
 * @param transferAmount số tiền giao dịch (VND)
 * @param accumulated    số dư tích lũy
 * @param referenceCode  mã tham chiếu SePay
 */
public record SePayWebhookRequest(
        Long id,
        String gateway,
        String transactionDate,
        String accountNumber,
        String subAccount,
        String code,
        String content,
        String transferType,
        String description,
        Long transferAmount,
        Long accumulated,
        String referenceCode
) {
}
