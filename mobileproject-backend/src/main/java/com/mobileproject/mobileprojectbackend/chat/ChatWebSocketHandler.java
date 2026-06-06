package com.mobileproject.mobileprojectbackend.chat;

import com.mobileproject.mobileprojectbackend.chat.ai.GroqChatClient;
import com.mobileproject.mobileprojectbackend.notifications.FcmPushService;
import com.mobileproject.mobileprojectbackend.notifications.NotificationService;
import com.mobileproject.mobileprojectbackend.notifications.NotificationType;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * WebSocket handler xử lý tin nhắn chat real-time giữa cặp đôi.
 *
 * <p><strong>Message protocol (JSON):</strong></p>
 * <ul>
 *   <li><b>Client → Server:</b>
 *     <ul>
 *       <li>{@code {"type":"message","text":"..."}} – gửi tin nhắn</li>
 *       <li>{@code {"type":"history"}} – yêu cầu load lại lịch sử</li>
 *     </ul>
 *   </li>
 *   <li><b>Server → Client:</b>
 *     <ul>
 *       <li>{@code {"type":"chat_history","messages":[...],"partnerUsername":"...","partnerAvatarUrl":"..."}} – lịch sử</li>
 *       <li>{@code {"type":"chat_message","id":"...","text":"...","mine":true/false,...}} – tin nhắn mới</li>
 *       <li>{@code {"type":"chat_error","message":"..."}} – lỗi</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <p><strong>Connection lifecycle:</strong></p>
 * <ol>
 *   <li>{@code afterConnectionEstablished} – thêm session vào pool theo coupleId, gửi lịch sử 50 tin nhắn gần nhất</li>
 *   <li>{@code handleTextMessage} – parse JSON, lưu DB, broadcast cho cả 2 user, gửi FCM push, trigger AI nếu có @MiniAI</li>
 *   <li>{@code afterConnectionClosed} – xóa session khỏi pool</li>
 * </ol>
 *
 * <p><strong>AI integration:</strong> Khi tin nhắn chứa {@code @MiniAI} hoặc {@code @MimIAI},
 * handler gọi {@link GroqChatClient} bất đồng bộ (CompletableFuture) để tạo phản hồi AI.
 * Context gửi cho AI bao gồm 20 tin nhắn gần nhất, phân loại role (user/assistant/system).</p>
 *
 * <p><strong>FCM push:</strong> Mỗi tin nhắn gửi thành công đều trigger FCM push cho người nhận
 * qua {@link FcmPushService}.</p>
 */
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    private static final String[] AI_MENTIONS_LOWER = { "@miniai", "@mimiai" };
    private static final String AI_SENDER_USER_ID = "ai:mimi";
    private static final String AI_USERNAME = "MiniAI";
    private static final int AI_CONTEXT_LIMIT = 20;

    private final ChatMessageRepository chatMessageRepository;
    private final ObjectMapper objectMapper;
    private final GroqChatClient groqChatClient;
    private final FcmPushService fcmPushService;
    private final NotificationService notificationService;

    private final Map<String, CopyOnWriteArraySet<WebSocketSession>> sessionsByCoupleId = new ConcurrentHashMap<>();

    public ChatWebSocketHandler(
            ChatMessageRepository chatMessageRepository,
            ObjectMapper objectMapper,
            GroqChatClient groqChatClient,
            FcmPushService fcmPushService,
            NotificationService notificationService) {
        this.chatMessageRepository = chatMessageRepository;
        this.objectMapper = objectMapper;
        this.groqChatClient = groqChatClient;
        this.fcmPushService = fcmPushService;
        this.notificationService = notificationService;
    }

    /**
     * Khi WebSocket kết nối thành công: thêm session vào pool coupleId, gửi lịch sử chat.
     */
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

    /**
     * Xử lý tin nhắn từ client: lưu DB, broadcast, FCM push, trigger AI nếu cần.
     */
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

            String senderDisplayName = usernameOf(session);
            String previewText = saved.getText().length() > 60
                    ? saved.getText().substring(0, 60) + "..."
                    : saved.getText();

            // Lưu thông báo vào DB để người nhận thấy khi mở app sau
            notificationService.createAndPush(
                    receiverUserId,
                    NotificationType.CHAT_MESSAGE,
                    senderDisplayName != null ? senderDisplayName + " đã nhắn tin" : "Tin nhắn mới",
                    previewText);

            // Always emit FCM push and let the client decide foreground/background
            // presentation.
            fcmPushService.sendChatMessagePush(
                    receiverUserId,
                    coupleId,
                    senderDisplayName,
                    saved.getText(),
                    saved.getId(),
                    saved.getCreatedAt());

            if (containsAiMention(saved.getText())) {
                triggerAiReplyAsync(session, coupleId, saved);
            }
        } catch (Exception exception) {
            LOGGER.debug("Failed to persist/broadcast chat message", exception);
            sendError(session, "Không thể gửi tin nhắn. Vui lòng thử lại.");
        }
    }

    /**
     * Khi WebSocket đóng: xóa session khỏi pool coupleId.
     */
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

    /**
     * Xử lý lỗi transport: log và đóng session.
     */
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
        envelope.put("partnerAvatarUrl", partnerAvatarUrlOf(session));
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
        String text = value.asString(null);
        if (text == null || text.isBlank()) {
            return null;
        }
        return text;
    }

    private Map<String, Object> toPayload(ChatMessage message, WebSocketSession session) {
        String myUserId = userIdOf(session);
        String myUsername = usernameOf(session);
        String partnerUsername = partnerUsernameOf(session);
        String myAvatarUrl = avatarUrlOf(session);
        String partnerAvatarUrl = partnerAvatarUrlOf(session);

        boolean isAi = AI_SENDER_USER_ID.equals(message.getSenderUserId());
        boolean mine = !isAi && myUserId != null && myUserId.equals(message.getSenderUserId());
        String senderUsername;
        String senderAvatarUrl;
        if (isAi) {
            senderUsername = AI_USERNAME;
            senderAvatarUrl = null;
        } else if (mine) {
            senderUsername = myUsername;
            senderAvatarUrl = myAvatarUrl;
        } else {
            senderUsername = partnerUsername;
            senderAvatarUrl = partnerAvatarUrl;
        }

        Map<String, Object> dto = new HashMap<>();
        dto.put("id", message.getId());
        dto.put("text", message.getText());
        dto.put("senderUsername", senderUsername);
        dto.put("senderAvatarUrl", senderAvatarUrl);
        dto.put("mine", mine);
        dto.put("createdAt",
                message.getCreatedAt() != null ? message.getCreatedAt().toString() : Instant.now().toString());
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

    private String avatarUrlOf(WebSocketSession session) {
        return session == null ? null : (String) session.getAttributes().get("avatarUrl");
    }

    private String partnerAvatarUrlOf(WebSocketSession session) {
        return session == null ? null : (String) session.getAttributes().get("partnerAvatarUrl");
    }

    private boolean containsAiMention(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String lowered = text.toLowerCase();
        for (String mention : AI_MENTIONS_LOWER) {
            if (mention != null && !mention.isBlank() && lowered.contains(mention)) {
                return true;
            }
        }
        return false;
    }

    private String stripAiMention(String text) {
        if (text == null) {
            return "";
        }
        String lowered = text.toLowerCase();

        int index = -1;
        String matchedMention = null;
        for (String mention : AI_MENTIONS_LOWER) {
            if (mention == null || mention.isBlank()) {
                continue;
            }
            int found = lowered.indexOf(mention);
            if (found >= 0 && (index < 0 || found < index)) {
                index = found;
                matchedMention = mention;
            }
        }

        if (index < 0 || matchedMention == null) {
            return text.trim();
        }

        String after = text.substring(index + matchedMention.length()).trim();
        if (!after.isBlank()) {
            String cleaned = after.replaceFirst("^[,.:;\\-]+\\s*", "");
            return cleaned.replaceAll("\\s{2,}", " ").trim();
        }

        String before = text.substring(0, index).trim();
        return before.replaceAll("\\s{2,}", " ").trim();
    }

    private void triggerAiReplyAsync(WebSocketSession session, String coupleId, ChatMessage latestUserMessage) {
        String prompt = stripAiMention(latestUserMessage.getText());
        if (prompt.isBlank()) {
            sendError(session, "Bạn hãy nhập nội dung sau @MiniAI để mình trả lời nhé.");
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                List<ChatMessage> recent = new ArrayList<>(
                        chatMessageRepository.findTop50ByCoupleIdOrderByCreatedAtDesc(coupleId));
                Collections.reverse(recent);
                List<GroqChatClient.Message> aiMessages = buildAiContext(session, recent, latestUserMessage.getId(),
                        prompt);

                String aiReply = groqChatClient.chat(aiMessages);
                if (aiReply.isBlank()) {
                    sendError(session, "MiniAI hiện chưa trả lời được. Vui lòng thử lại.");
                    return;
                }

                ChatMessage aiEntity = new ChatMessage();
                aiEntity.setCoupleId(coupleId);
                aiEntity.setSenderUserId(AI_SENDER_USER_ID);
                aiEntity.setReceiverUserId(userIdOf(session));
                aiEntity.setText(aiReply);
                aiEntity.setCreatedAt(Instant.now());

                ChatMessage savedAi = chatMessageRepository.save(aiEntity);
                broadcastMessage(coupleId, savedAi);
            } catch (GroqChatClient.GroqApiException exception) {
                sendError(session, mapGroqError(exception));
            } catch (IllegalStateException exception) {
                String message = exception.getMessage();
                if (message == null || message.isBlank()) {
                    sendError(session, "Không thể gọi MiniAI lúc này. Vui lòng thử lại.");
                } else {
                    sendError(session, message);
                }
            } catch (Exception exception) {
                LOGGER.debug("Failed to generate MiniAI reply", exception);
                sendError(session, "Không thể gọi MiniAI lúc này. Vui lòng thử lại.");
            }
        });
    }

    private String mapGroqError(GroqChatClient.GroqApiException exception) {
        int status = exception == null ? 0 : exception.statusCode();
        String detail = exception == null ? "" : exception.groqMessage();

        String message;
        if (status == 401 || status == 403) {
            message = "Groq API key không hợp lệ hoặc đã hết hạn. Hãy cập nhật GROQ_API_KEY trên backend.";
        } else if (status == 429) {
            message = "MiniAI đang quá tải hoặc hết quota. Vui lòng thử lại sau.";
        } else if (status >= 500) {
            message = "Groq đang gặp lỗi tạm thời. Vui lòng thử lại sau.";
        } else if (status > 0) {
            message = "Không thể gọi MiniAI (HTTP " + status + ").";
        } else {
            message = "Không thể gọi MiniAI lúc này. Vui lòng thử lại.";
        }

        if (detail != null && !detail.isBlank()) {
            String clipped = detail.length() > 140 ? detail.substring(0, 140) + "..." : detail;
            return message + " (" + clipped + ")";
        }
        return message;
    }

    private List<GroqChatClient.Message> buildAiContext(
            WebSocketSession session,
            List<ChatMessage> recentMessages,
            String latestMessageId,
            String latestPrompt) {
        String myUserId = userIdOf(session);
        String partnerUserId = partnerUserIdOf(session);

        List<GroqChatClient.Message> messages = new ArrayList<>();
        messages.add(new GroqChatClient.Message(
                "system",
                "Bạn là MiniAI, trợ lý thân thiện trong cuộc trò chuyện của một cặp đôi. Trả lời ngắn gọn, rõ ràng, bằng tiếng Việt. Không nhắc lại email/username trong câu trả lời. Nếu thiếu thông tin, hãy hỏi lại 1 câu."));

        if (recentMessages == null || recentMessages.isEmpty()) {
            messages.add(new GroqChatClient.Message("user", latestPrompt));
            return messages;
        }

        int startIndex = Math.max(0, recentMessages.size() - AI_CONTEXT_LIMIT);
        for (int i = startIndex; i < recentMessages.size(); i++) {
            ChatMessage chatMessage = recentMessages.get(i);
            if (chatMessage == null || chatMessage.getText() == null || chatMessage.getText().isBlank()) {
                continue;
            }

            String senderId = chatMessage.getSenderUserId();
            boolean isAi = AI_SENDER_USER_ID.equals(senderId);
            String contentText;
            if (latestMessageId != null && latestMessageId.equals(chatMessage.getId())) {
                contentText = latestPrompt;
            } else {
                contentText = chatMessage.getText();
            }

            if (contentText == null || contentText.isBlank()) {
                continue;
            }

            if (isAi) {
                messages.add(new GroqChatClient.Message("assistant", contentText));
                continue;
            }

            if (latestMessageId != null && latestMessageId.equals(chatMessage.getId())) {
                messages.add(new GroqChatClient.Message("user", contentText));
                continue;
            }

            String label;
            if (senderId != null && senderId.equals(myUserId)) {
                label = "Bạn";
            } else if (senderId != null && senderId.equals(partnerUserId)) {
                label = "Partner";
            } else {
                label = "Người dùng";
            }

            String content = (label == null || label.isBlank())
                    ? contentText
                    : (label + ": " + contentText);

            messages.add(new GroqChatClient.Message("user", content));
        }

        return messages;
    }

    private record IncomingChat(String type, String text) {
    }
}
