package com.mobileproject.mobileprojectbackend.topup.dto;

import java.time.Instant;

/**
 * DTO response trả về kết quả tạo/truy vấn yêu cầu nạp tiền.
 *
 * @param success        {@code true} nếu thao tác thành công
 * @param message        thông báo mô tả kết quả
 * @param id             ID yêu cầu nạp tiền
 * @param coupleId       ID cặp đôi
 * @param amount         số tiền nạp (VND)
 * @param status         trạng thái yêu cầu ("PENDING", "PROCESSING", "PAID", "FAILED")
 * @param transferCode   mã chuyển khoản để người dùng chuyển tiền
 * @param bankId         mã ngân hàng nhận tiền
 * @param bankName       tên ngân hàng
 * @param accountNumber  số tài khoản nhận
 * @param accountName    tên chủ tài khoản
 * @param transferContent nội dung chuyển khoản
 * @param qrContent      nội dung QR
 * @param qrImageUrl     URL hình ảnh mã QR
 * @param currentBalance số dư ví hiện tại
 * @param createdAt      thời điểm tạo yêu cầu
 * @param paidAt         thời điểm thanh toán thành công (null nếu chưa thanh toán)
 */
public record TopUpResponse(
        boolean success,
        String message,
        String id,
        String coupleId,
        Long amount,
        String status,
        String transferCode,
        String bankId,
        String bankName,
        String accountNumber,
        String accountName,
        String transferContent,
        String qrContent,
        String qrImageUrl,
        Long currentBalance,
        Instant createdAt,
        Instant paidAt
) {
    /**
     * Tạo response thất bại.
     *
     * @param message thông báo lỗi
     * @return {@link TopUpResponse} thất bại với tất cả trường null
     */
    public static TopUpResponse failure(String message) {
        return new TopUpResponse(false, message, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null);
    }

    /**
     * Alias cho {@code id} – trả về ID yêu cầu nạp tiền.
     */
    public String topUpRequestId() {
        return id;
    }

    /**
     * Alias cho {@code bankId} – trả về mã ngân hàng.
     */
    public String bankCode() {
        return bankId;
    }

    /**
     * Alias cho {@code qrImageUrl} – trả về URL mã QR.
     */
    public String qrUrl() {
        return qrImageUrl;
    }
}
