package com.mobileproject.mobileprojectbackend.topup;

/**
 * DTO kết quả xử lý webhook nạp tiền từ SePay.
 *
 * @param success {@code true} nếu webhook được xử lý thành công
 * @param message thông báo mô tả kết quả xử lý
 */
public record TopUpWebhookResult(boolean success, String message) {

    /**
     * Tạo kết quả thành công.
     *
     * @param message thông báo
     * @return {@link TopUpWebhookResult} thành công
     */
    public static TopUpWebhookResult success(String message) {
        return new TopUpWebhookResult(true, message);
    }

    /**
     * Tạo kết quả thất bại.
     *
     * @param message thông báo lỗi
     * @return {@link TopUpWebhookResult} thất bại
     */
    public static TopUpWebhookResult failure(String message) {
        return new TopUpWebhookResult(false, message);
    }
}
