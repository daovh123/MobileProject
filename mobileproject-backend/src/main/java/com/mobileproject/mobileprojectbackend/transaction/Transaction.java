package com.mobileproject.mobileprojectbackend.transaction;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

/**
 * Entity đại diện cho một giao dịch tài chính (thu/chi) của cặp đôi.
 *
 * <p>Lưu trong MongoDB collection {@code transactions}.
 * Mỗi giao dịch gắn với một cặp đôi thông qua {@code coupleId}.</p>
 *
 * <h3>Indexes:</h3>
 * <ul>
 *   <li>{@code id_couple} – indexed, truy vấn nhanh theo cặp đôi</li>
 * </ul>
 *
 * <h3>Mối quan hệ:</h3>
 * <ul>
 *   <li>{@code coupleId} → tham chiếu đến {@code CoupleInfo.id}</li>
 * </ul>
 */
@Document(collection = "transactions")
public class Transaction {

    /**
     * ID duy nhất của giao dịch (MongoDB ObjectId).
     */
    @Id
    private String id;

    /**
     * ID của cặp đôi sở hữu giao dịch này.
     * Được index để truy vấn nhanh.
     */
    @Indexed
    @Field("id_couple")
    private String coupleId;

    /** Số tiền giao dịch (đơn vị: VND). */
    private Long amount;

    /** Loại giao dịch: {@link TransactionType#INCOME} hoặc {@link TransactionType#EXPENSE}. */
    private TransactionType type;

    /** Danh mục giao dịch (ví dụ: "Ăn uống", "Di chuyển", "INCOME"). */
    private String category;

    /** Ghi chú thêm cho giao dịch. */
    private String note;

    /** Thời điểm tạo giao dịch (UTC). */
    @Field("created_at")
    private Instant createdAt;

    /**
     * Constructor mặc định bắt buộc bởi MongoDB driver.
     */
    public Transaction() {
    }

    /**
     * Tạo giao dịch mới với thời gian tạo là thời điểm hiện tại.
     *
     * @param coupleId ID cặp đôi
     * @param amount   số tiền (VND)
     * @param type     loại giao dịch (INCOME / EXPENSE)
     * @param category danh mục
     * @param note     ghi chú
     */
    public Transaction(String coupleId, Long amount, TransactionType type, String category, String note) {
        this.coupleId = coupleId;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.note = note;
        this.createdAt = Instant.now();
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

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}