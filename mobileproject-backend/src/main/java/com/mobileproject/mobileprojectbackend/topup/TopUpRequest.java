package com.mobileproject.mobileprojectbackend.topup;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

/**
 * Entity đại diện cho yêu cầu nạp tiền vào ví chung thông qua SePay.
 *
 * <p>Lưu trong MongoDB collection {@code top_up_requests}.
 * Mỗi yêu cầu nạp tiền gắn với một cặp đôi và có mã chuyển khoản duy nhất
 * để đối soát với webhook từ SePay.</p>
 *
 * <h3>Indexes:</h3>
 * <ul>
 *   <li>{@code id_couple} – indexed, truy vấn nhanh theo cặp đôi</li>
 *   <li>{@code transferCode} – unique, đảm bảo mã chuyển khoản không trùng lặp</li>
 *   <li>{@code sepayId} – unique sparse, đối soát webhook SePay theo ID giao dịch</li>
 *   <li>{@code referenceCode} – unique sparse, đối soát webhook SePay theo mã tham chiếu</li>
 * </ul>
 *
 * <h3>Luồng trạng thái:</h3>
 * <pre>PENDING → PROCESSING → PAID
 *                   ↘ FAILED</pre>
 *
 * <h3>Mối quan hệ:</h3>
 * <ul>
 *   <li>{@code coupleId} → tham chiếu đến {@code CoupleInfo.id}</li>
 *   <li>{@code transactionId} → tham chiếu đến {@code Transaction.id} (sau khi thanh toán)</li>
 * </ul>
 */
@Document(collection = "top_up_requests")
public class TopUpRequest {

    /** ID duy nhất của yêu cầu nạp tiền (MongoDB ObjectId). */
    @Id
    private String id;

    /** ID cặp đôi yêu cầu nạp tiền. */
    @Indexed
    @Field("id_couple")
    private String coupleId;

    /** Số tiền cần nạp (VND). */
    private Long amount;

    /** Mã chuyển khoản duy nhất, dùng để đối soát với SePay webhook. */
    @Indexed(unique = true)
    private String transferCode;

    /** Mã ngân hàng (ví dụ: "MBBank"). */
    private String bankCode;
    /** Tên ngân hàng hiển thị cho người dùng. */
    private String bankName;
    /** Số tài khoản nhận tiền. */
    private String accountNumber;
    /** Tên chủ tài khoản nhận tiền. */
    private String accountName;
    /** Nội dung chuyển khoản (bao gồm mã chuyển khoản + ghi chú). */
    private String transferContent;
    /** Nội dung mã QR (dùng để tạo QR trên client). */
    private String qrContent;
    /** URL hình ảnh mã QR từ SePay. */
    private String qrUrl;
    /** Trạng thái yêu cầu nạp tiền. */
    private TopUpRequestStatus status;

    /** ID giao dịch từ SePay (dùng để deduplicate webhook). */
    @Indexed(unique = true, sparse = true)
    private Long sepayId;

    /** Mã tham chiếu từ SePay (dùng để deduplicate webhook). */
    @Indexed(unique = true, sparse = true)
    private String referenceCode;

    /** ID giao dịch {@code Transaction} sau khi nạp thành công. */
    private String transactionId;
    /** Nội dung webhook từ SePay (lưu để debug). */
    private String webhookContent;
    /** Mô tả từ webhook SePay. */
    private String webhookDescription;
    /** Lỗi gần nhất nếu xử lý thất bại. */
    private String lastError;
    /** Thời điểm tạo yêu cầu. */
    private Instant createdAt;
    /** Thời điểm cập nhật gần nhất. */
    private Instant updatedAt;
    /** Thời điểm nạp tiền thành công. */
    private Instant paidAt;

    /**
     * Constructor mặc định bắt buộc bởi MongoDB driver.
     */
    public TopUpRequest() {
    }

    /**
     * Tạo yêu cầu nạp tiền mới với trạng thái mặc định là {@code PENDING}.
     *
     * @param coupleId        ID cặp đôi
     * @param amount          số tiền nạp (VND)
     * @param transferCode    mã chuyển khoản duy nhất
     * @param bankCode        mã ngân hàng
     * @param bankName        tên ngân hàng
     * @param accountNumber   số tài khoản nhận
     * @param accountName     tên chủ tài khoản
     * @param transferContent nội dung chuyển khoản
     * @param qrContent       nội dung QR
     * @param qrUrl           URL mã QR
     */
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
