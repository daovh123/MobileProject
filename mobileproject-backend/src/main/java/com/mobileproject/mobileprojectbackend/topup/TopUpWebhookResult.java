package com.mobileproject.mobileprojectbackend.topup;

public record TopUpWebhookResult(boolean success, String message) {

    public static TopUpWebhookResult success(String message) {
        return new TopUpWebhookResult(true, message);
    }

    public static TopUpWebhookResult failure(String message) {
        return new TopUpWebhookResult(false, message);
    }
}
