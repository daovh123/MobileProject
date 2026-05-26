package com.mobileproject.mobileprojectbackend.topup;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SePayProperties {

    private final String apiKey;
    private final String bankCode;
    private final String bankName;
    private final String accountNumber;
    private final String accountName;
    private final String transferPrefix;
    private final String qrTemplate;

    public SePayProperties(
            @Value("${sepay.api-key:}") String apiKey,
            @Value("${payment.sepay.webhook-api-key:}") String legacyApiKey,
            @Value("${sepay.bank-code:}") String bankCode,
            @Value("${payment.sepay.bank-bin:}") String legacyBankCode,
            @Value("${sepay.bank-name:}") String bankName,
            @Value("${payment.sepay.bank-name:}") String legacyBankName,
            @Value("${sepay.account-number:}") String accountNumber,
            @Value("${payment.sepay.account-number:}") String legacyAccountNumber,
            @Value("${sepay.account-name:}") String accountName,
            @Value("${payment.sepay.account-name:}") String legacyAccountName,
            @Value("${sepay.transfer-prefix:}") String transferPrefix,
            @Value("${payment.sepay.payment-code-prefix:}") String legacyTransferPrefix,
            @Value("${sepay.qr-template:}") String qrTemplate,
            @Value("${payment.sepay.qr-template:}") String legacyQrTemplate) {
        this.apiKey = valueOrFallback(apiKey, legacyApiKey);
        this.bankCode = valueOrFallback(valueOrFallback(bankCode, legacyBankCode), "MBBank");
        this.bankName = valueOrFallback(valueOrFallback(bankName, legacyBankName), "Demo Bank");
        this.accountNumber = valueOrFallback(valueOrFallback(accountNumber, legacyAccountNumber), "0123456789");
        this.accountName = valueOrFallback(valueOrFallback(accountName, legacyAccountName), "YOU AND ME WALLET");
        this.transferPrefix = valueOrFallback(valueOrFallback(transferPrefix, legacyTransferPrefix), "YMW");
        this.qrTemplate = valueOrFallback(valueOrFallback(qrTemplate, legacyQrTemplate), "compact");
    }

    public String apiKey() {
        return apiKey;
    }

    public String bankCode() {
        return bankCode;
    }

    public String bankName() {
        return bankName;
    }

    public String accountNumber() {
        return accountNumber;
    }

    public String accountName() {
        return accountName;
    }

    public String transferPrefix() {
        return transferPrefix;
    }

    public String qrTemplate() {
        return qrTemplate;
    }

    private String valueOrFallback(String value, String fallback) {
        return value != null && !value.isBlank() ? value : fallback;
    }
}
