package com.mobileproject.mobileprojectbackend.goal.dto;

public record ContributeFromWalletRequest(
        Long amount,
        String note
) {
}