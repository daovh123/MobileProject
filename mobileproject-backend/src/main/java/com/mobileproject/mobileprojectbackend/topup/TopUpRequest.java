package com.mobileproject.mobileprojectbackend.topup;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Document(collection = "top_up_requests")
public class TopUpRequest {

    @Id
    private String id;

    @Indexed
    @Field("id_couple")
    private String coupleId;

    private Long amount;

    @Indexed(unique = true)
    private String transferCode;

    private String bankCode;
    private String bankName;
    private String accountNumber;
    private String accountName;
    private String transferContent;
    private String qrContent;
    private String qrUrl;
    private TopUpRequestStatus status;

    @Indexed(unique = true, sparse = true)
    private Long sepayId;

    @Indexed(unique = true, sparse = true)
    private String referenceCode;

    private String transactionId;
    private String webhookContent;
    private String webhookDescription;
    private String lastError;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant paidAt;

    public TopUpRequest() {
    }

    public TopUpRequest(String coupleId, Long amount, String transferCode, String bankCode, String bankName,
                        String accountNumber, String accountName, String transferContent, String qrContent,
                        String qrUrl) {
        this.coupleId = coupleId;
        this.amount = amount;
        this.transferCode = transferCode;
        this.bankCode = bankCode;
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.accountName = accountName;
        this.transferContent = transferContent;
        this.qrContent = qrContent;
        this.qrUrl = qrUrl;
        this.status = TopUpRequestStatus.PENDING;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCoupleId() {
        return coupleId;
    }

    public void setCoupleId(String coupleId) {
        this.coupleId = coupleId;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getTransferCode() {
        return transferCode;
    }

    public void setTransferCode(String transferCode) {
        this.transferCode = transferCode;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getTransferContent() {
        return transferContent;
    }

    public void setTransferContent(String transferContent) {
        this.transferContent = transferContent;
    }

    public String getQrContent() {
        return qrContent;
    }

    public void setQrContent(String qrContent) {
        this.qrContent = qrContent;
    }

    public String getQrUrl() {
        return qrUrl;
    }

    public void setQrUrl(String qrUrl) {
        this.qrUrl = qrUrl;
    }

    public TopUpRequestStatus getStatus() {
        return status;
    }

    public void setStatus(TopUpRequestStatus status) {
        this.status = status;
    }

    public Long getSepayId() {
        return sepayId;
    }

    public void setSepayId(Long sepayId) {
        this.sepayId = sepayId;
    }

    public String getReferenceCode() {
        return referenceCode;
    }

    public void setReferenceCode(String referenceCode) {
        this.referenceCode = referenceCode;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getWebhookContent() {
        return webhookContent;
    }

    public void setWebhookContent(String webhookContent) {
        this.webhookContent = webhookContent;
    }

    public String getWebhookDescription() {
        return webhookDescription;
    }

    public void setWebhookDescription(String webhookDescription) {
        this.webhookDescription = webhookDescription;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Instant paidAt) {
        this.paidAt = paidAt;
    }
}
