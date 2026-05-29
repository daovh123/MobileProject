package com.mobileproject.mobileprojectbackend.payout;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "payout_requests")
public class PayoutRequest {

    @Id
    private String id;

    private String coupleId;
    private Long amount;

    @Indexed(unique = true)
    private String transferCode;

    private PayoutRequestStatus status;

    private Long sepayId;
    private String referenceCode;
    private String webhookContent;
    private String webhookDescription;

    private String transactionId;
    private String lastError;

    private Instant createdAt;
    private Instant updatedAt;
    private Instant paidAt;

    public PayoutRequest() {
        // for Mongo
    }

    public PayoutRequest(String coupleId, Long amount, String transferCode) {
        this.coupleId = coupleId;
        this.amount = amount;
        this.transferCode = transferCode;
        this.status = PayoutRequestStatus.PENDING;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public String getId() {
        return id;
    }

    public String getCoupleId() {
        return coupleId;
    }

    public Long getAmount() {
        return amount;
    }

    public String getTransferCode() {
        return transferCode;
    }

    public PayoutRequestStatus getStatus() {
        return status;
    }

    public void setStatus(PayoutRequestStatus status) {
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

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
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

