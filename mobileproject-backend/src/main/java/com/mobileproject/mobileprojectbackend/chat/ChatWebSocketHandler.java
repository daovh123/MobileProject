package com.mobileproject.mobileprojectbackend.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    private final ChatMessageRepository chatMessageRepository;
    private final ObjectMapper objectMapper;

    private final Map<String, CopyOnWriteArraySet<WebSocketSession>> sessionsByCoupleId = new ConcurrentHashMap<>();

    public ChatWebSocketHandler(ChatMessageRepository chatMessageRepository, ObjectMapper objectMapper) {
        this.chatMessageRepository = chatMessageRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String coupleId = coupleIdOf(session);
        if (coupleId == null) {
            safeClose(session, CloseStatus.BAD_DATA);
            return;
        }

        sessionsByCoupleId
                .computeIfAbsent(coupleId, ignored -> new CopyOnWriteArraySet<>())
                .add(session);

        sendHistory(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String coupleId = coupleIdOf(session);
        String senderUserId = userIdOf(session);
        String receiverUserId = partnerUserIdOf(session);

        if (coupleId == null || senderUserId == null || receiverUserId == null) {
            safeClose(session, CloseStatus.BAD_DATA);
            return;
        }

        IncomingChat payload = parseIncoming(message.getPayload());
        if (payload == null) {
            return;
        }

        if ("history".equalsIgnoreCase(payload.type)) {
            sendHistory(session);
            return;
        }

        if (payload.text == null || payload.text.isBlank()) {
            sendError(session, "Bạn cần nhập nội dung tin nhắn");
            return;
        }

        try {
            ChatMessage entity = new ChatMessage();
            entity.setCoupleId(coupleId);
            entity.setSenderUserId(senderUserId);
            entity.setReceiverUserId(receiverUserId);
            entity.setText(payload.text.trim());
            entity.setCreatedAt(Instant.now());

            ChatMessage saved = chatMessageRepository.save(entity);
            broadcastMessage(coupleId, saved);
        } catch (Exception exception) {
            LOGGER.debug("Failed to persist/broadcast chat message", exception);
            sendError(session, "Không thể gửi tin nhắn. Vui lòng thử lại.");
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String coupleId = coupleIdOf(session);
        if (coupleId == null) {
            return;
        }

        CopyOnWriteArraySet<WebSocketSession> sessions = sessionsByCoupleId.get(coupleId);
        if (sessions == null) {
            return;
        }

        sessions.remove(session);
        if (sessions.isEmpty()) {
            sessionsByCoupleId.remove(coupleId);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        LOGGER.debug("WebSocket transport error", exception);
        safeClose(session, CloseStatus.SERVER_ERROR);
    }

    private void sendHistory(WebSocketSession session) {
        String coupleId = coupleIdOf(session);
        if (coupleId == null) {
            return;
        }

        List<ChatMessage> messages;
        try {
            messages = new ArrayList<>(chatMessageRepository.findTop50ByCoupleIdOrderByCreatedAtDesc(coupleId));
        } catch (Exception exception) {
            LOGGER.debug("Failed to load chat history", exception);
            sendError(session, "Không thể tải tin nhắn. Vui lòng thử lại.");
            return;
        }

        Collections.reverse(messages);

        List<Map<String, Object>> payload = messages.stream()
                .map(message -> toPayload(message, session))
                .toList();

        Map<String, Object> envelope = new HashMap<>();
        envelope.put("type", "chat_history");
        envelope.put("partnerUsername", partnerUsernameOf(session));
        envelope.put("messages", payload);

        String json = writeJson(envelope);
        if (json != null) {
            safeSend(session, json);
        }
    }

    private void broadcastMessage(String coupleId, ChatMessage message) {
        CopyOnWriteArraySet<WebSocketSession> sessions = sessionsByCoupleId.get(coupleId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        for (WebSocketSession session : sessions) {
            if (session == null || !session.isOpen()) {
                continue;
            }

            Map<String, Object> payload = toPayload(message, session);
            payload.put("type", "chat_message");

            String json = writeJson(payload);
            if (json == null) {
                continue;
            }

            safeSend(session, json);
        }
    }

    private void sendError(WebSocketSession session, String errorMessage) {
        if (session == null || !session.isOpen()) {
            return;
        }

        Map<String, Object> envelope = new HashMap<>();
        envelope.put("type", "chat_error");
        envelope.put("message", errorMessage);
        envelope.put("updatedAt", Instant.now().toString());

        String json = writeJson(envelope);
        if (json != null) {
            safeSend(session, json);
        }
    }

    private IncomingChat parseIncoming(String payload) {
        if (payload == null || payload.isBlank()) {
            return null;
        }

        try {
            JsonNode node = objectMapper.readTree(payload);
            String type = readText(node, "type");
            if (type == null) {
                type = "message";
            }
            if (!"message".equalsIgnoreCase(type)
                    && !"chat_message".equalsIgnoreCase(type)
                    && !"history".equalsIgnoreCase(type)) {
                return null;
            }

            String text = readText(node, "text");
            if (text == null) {
                text = readText(node, "message");
            }
            return new IncomingChat(type, text);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String readText(JsonNode node, String field) {
        if (node == null || field == null) {
            return null;
        }
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        String text = value.asText(null);
        if (text == null || text.isBlank()) {
            return null;
        }
        return text;
    }

    private Map<String, Object> toPayload(ChatMessage message, WebSocketSession session) {
        String myUserId = userIdOf(session);
        String myUsername = usernameOf(session);
        String partnerUsername = partnerUsernameOf(session);

        boolean mine = myUserId != null && myUserId.equals(message.getSenderUserId());
        String senderUsername = mine ? myUsername : partnerUsername;

        Map<String, Object> dto = new HashMap<>();
        dto.put("id", message.getId());
        dto.put("text", message.getText());
        dto.put("senderUsername", senderUsername);
        dto.put("mine", mine);
        dto.put("createdAt", message.getCreatedAt() != null ? message.getCreatedAt().toString() : Instant.now().toString());
        return dto;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            LOGGER.debug("Failed to serialize WebSocket message", exception);
            return null;
        }
    }

    private void safeSend(WebSocketSession session, String payload) {
        try {
            session.sendMessage(new TextMessage(payload));
        } catch (Exception ignored) {
            // best-effort
        }
    }

    private void safeClose(WebSocketSession session, CloseStatus status) {
        try {
            session.close(status);
        } catch (Exception ignored) {
            // best-effort
        }
    }

    private String coupleIdOf(WebSocketSession session) {
        return session == null ? null : (String) session.getAttributes().get("coupleId");
    }

    private String userIdOf(WebSocketSession session) {
        return session == null ? null : (String) session.getAttributes().get("userId");
    }

    private String usernameOf(WebSocketSession session) {
        return session == null ? null : (String) session.getAttributes().get("username");
    }

    private String partnerUserIdOf(WebSocketSession session) {
        return session == null ? null : (String) session.getAttributes().get("partnerUserId");
    }

    private String partnerUsernameOf(WebSocketSession session) {
        return session == null ? null : (String) session.getAttributes().get("partnerUsername");
    }

    private record IncomingChat(String type, String text) {
    }
}
