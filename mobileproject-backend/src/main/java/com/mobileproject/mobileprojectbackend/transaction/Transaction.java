package com.mobileproject.mobileprojectbackend.transaction;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Document(collection = "transactions")
public class Transaction {

    @Id
    private String id;

    @Indexed
    @Field("id_couple")
    private String coupleId;

    private Long amount;

    private TransactionType type;

    private String category;

    private String note;

    @Field("created_at")
    private Instant createdAt;

    public Transaction() {
    }

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