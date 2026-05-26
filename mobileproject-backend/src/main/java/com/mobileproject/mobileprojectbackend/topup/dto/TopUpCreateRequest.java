package com.mobileproject.mobileprojectbackend.topup.dto;

public record TopUpCreateRequest(
        String coupleId,
        Long amount,
        String bankId,
        String bankName,
        String note
) {
}
