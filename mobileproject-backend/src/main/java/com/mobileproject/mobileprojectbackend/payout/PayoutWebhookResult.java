package com.mobileproject.mobileprojectbackend.payout;

public record PayoutWebhookResult(boolean success, String message) {

    public static PayoutWebhookResult success(String message) {
        return new PayoutWebhookResult(true, message);
    }

    public static PayoutWebhookResult failure(String message) {
        return new PayoutWebhookResult(false, message);
    }
}

