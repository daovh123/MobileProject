package com.mobileproject.mobileprojectbackend.payout;

/**
 * DTO kết quả xử lý webhook rút tiền từ SePay.
 *
 * @param success {@code true} nếu webhook được xử lý thành công
 * @param message thông báo mô tả kết quả xử lý
 */
public record PayoutWebhookResult(boolean success, String message) {

    /**
     * Tạo kết quả thành công.
     *
     * @param message thông báo
     * @return {@link PayoutWebhookResult} thành công
     */
    public static PayoutWebhookResult success(String message) {
        return new PayoutWebhookResult(true, message);
    }

    /**
     * Tạo kết quả thất bại.
     *
     * @param message thông báo lỗi
     * @return {@link PayoutWebhookResult} thất bại
     */
    public static PayoutWebhookResult failure(String message) {
        return new PayoutWebhookResult(false, message);
    }
}

