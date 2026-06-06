package com.mobileproject.mobileprojectbackend.chat;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Entity đại diện cho một tin nhắn chat giữa cặp đôi.
 * Ánh xạ tới collection {@code chat_messages}.
 *
 * <p><strong>Compound index:</strong> {@code coupleId + createdAt} để tối ưu
 * truy vấn lịch sử tin nhắn theo couple, sắp xếp theo thời gian.</p>
 */
@Document(collection = "chat_messages")
@CompoundIndex(name = "couple_created_at_idx", def = "{'coupleId': 1, 'createdAt': 1}")
public class ChatMessage {

    @Id
    private String id;

    private String coupleId;
    private String senderUserId;
    private String receiverUserId;
    private String text;
    private Instant createdAt;

    public ChatMessage() {
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

    public String getSenderUserId() {
        return senderUserId;
    }

    public void setSenderUserId(String senderUserId) {
        this.senderUserId = senderUserId;
    }

    public String getReceiverUserId() {
        return receiverUserId;
    }

    public void setReceiverUserId(String receiverUserId) {
        this.receiverUserId = receiverUserId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
