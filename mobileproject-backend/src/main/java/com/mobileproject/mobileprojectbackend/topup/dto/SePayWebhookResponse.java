package com.mobileproject.mobileprojectbackend.topup.dto;

/**
 * DTO response trả về cho SePay sau khi xử lý webhook.
 *
 * @param success {@code true} nếu webhook được xử lý thành công
 */
public record SePayWebhookResponse(boolean success) {
}
