package com.mobileproject.mobileprojectbackend.topup;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Cấu hình thuộc tính cho tích hợp SePay (cổng thanh toán).
 *
 * <p>Hỗ trợ cả cấu hình mới ({@code sepay.*}) và legacy ({@code payment.sepay.*}).
 * Giá trị mới được ưu tiên, nếu trống sẽ dùng giá trị legacy.</p>
 *
 * <h3>Properties:</h3>
 * <ul>
 *   <li>{@code sepay.api-key} – API key để xác thực webhook từ SePay</li>
 *   <li>{@code sepay.bank-code} – Mã ngân hàng (mặc định: "MBBank")</li>
 *   <li>{@code sepay.bank-name} – Tên ngân hàng (mặc định: "Demo Bank")</li>
 *   <li>{@code sepay.account-number} – Số tài khoản nhận tiền (mặc định: "0123456789")</li>
 *   <li>{@code sepay.account-name} – Tên chủ tài khoản (mặc định: "YOU AND ME WALLET")</li>
 *   <li>{@code sepay.transfer-prefix} – Prefix mã chuyển khoản (mặc định: "YMW")</li>
 *   <li>{@code sepay.qr-template} – Template mã QR (mặc định: "compact")</li>
 * </ul>
 */
@Component
public class SePayProperties {

    /** API key để xác thực webhook từ SePay. */
    private final String apiKey;
    /** Mã ngân hàng (ví dụ: "MBBank"). */
    private final String bankCode;
    /** Tên ngân hàng hiển thị. */
    private final String bankName;
    /** Số tài khoản nhận tiền. */
    private final String accountNumber;
    /** Tên chủ tài khoản nhận tiền. */
    private final String accountName;
    /** Prefix cho mã chuyển khoản (mặc định: "YMW"). */
    private final String transferPrefix;
    /** Template mã QR từ SePay (mặc định: "compact"). */
    private final String qrTemplate;

    /**
     * Khởi tạo SePayProperties từ cấu hình application properties.
     * Hỗ trợ cả property mới ({@code sepay.*}) và legacy ({@code payment.sepay.*}).
     * Giá trị mới được ưu tiên; nếu cả hai trống → dùng giá trị mặc định.
     */
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
