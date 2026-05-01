package com.mobileproject.mobileprojectbackend.auth.dto;

public record WalletResponse(
        String idCouple,
        String walletName,
        Long totalBalance
) {
    public WalletResponse(String idCouple, Long totalBalance) {
        this(idCouple, "Ví chung", totalBalance);
    }
}