package com.mobileproject.mobileprojectbackend.payout;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Entity đại diện cho yêu cầu rút tiền (payout) từ ví chung ra ngân hàng qua SePay.
 *
 * <p>Lưu trong MongoDB collection {@code payout_requests}.
 * Tương tự TopUpRequest nhưng theo chiều ngược lại: tiền đi ra từ ví.</p>
 *
 * <h3>Indexes:</h3>
 * <ul>
 *   <li>{@code transferCode} – unique, mã chuyển khoản để đối soát với SePay webhook</li>
 * </ul>
 *
 * <h3>Luồng trạng thái:</h3>
 * <pre>PENDING → PROCESSING → PAID
 *                   ↘ FAILED</pre>
 *
 * <h3>Mối quan hệ:</h3>
 * <ul>
 *   <li>{@code coupleId} → tham chiếu đến {@code CoupleInfo.id}</li>
 *   <li>{@code transactionId} → tham chiếu đến {@code Transaction.id} (sau khi payout thành công)</li>
 * </ul>
 */
@Document(collection = "payout_requests")
public class PayoutRequest {

    /** ID duy nhất của yêu cầu rút tiền (MongoDB ObjectId). */
    @Id
    private String id;

    /** ID cặp đôi yêu cầu rút tiền. */
    private String coupleId;
    /** Số tiền rút (VND). */
    private Long amount;

    /** Mã chuyển khoản duy nhất, dùng để đối soát với SePay webhook. */
    @Indexed(unique = true)
    private String transferCode;

    /** Trạng thái yêu cầu rút tiền. */
    private PayoutRequestStatus status;

    /** ID giao dịch từ SePay (dùng để deduplicate webhook). */
    private Long sepayId;
    /** Mã tham chiếu từ SePay. */
    private String referenceCode;
    /** Nội dung webhook từ SePay (lưu để debug). */
    private String webhookContent;
    /** Mô tả từ webhook SePay. */
    private String webhookDescription;

    /** ID giao dịch {@code Transaction} sau khi payout thành công. */
    private String transactionId;
    /** Lỗi gần nhất nếu xử lý thất bại. */
    private String lastError;

    /** Thời điểm tạo yêu cầu. */
    private Instant createdAt;
    /** Thời điểm cập nhật gần nhất. */
    private Instant updatedAt;
    /** Thời điểm rút tiền thành công. */
    private Instant paidAt;

    /**
     * Constructor mặc định bắt buộc bởi MongoDB driver.
     */
    public PayoutRequest() {
        // for Mongo
    }

    /**
     * Tạo yêu cầu rút tiền mới với trạng thái mặc định là {@code PENDING}.
     *
     * @param coupleId     ID cặp đôi
     * @param amount       số tiền rút (VND)
     * @param transferCode mã chuyển khoản duy nhất
     */
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

