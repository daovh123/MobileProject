package com.mobileproject.mobileprojectbackend.chat;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Repository truy vấn collection {@code chat_messages}.
 */
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    /**
     * Lấy 50 tin nhắn gần nhất của couple (sắp xếp mới nhất trước).
     * Dùng để load lịch sử chat khi kết nối WebSocket.
     */
    List<ChatMessage> findTop50ByCoupleIdOrderByCreatedAtDesc(String coupleId);
}
