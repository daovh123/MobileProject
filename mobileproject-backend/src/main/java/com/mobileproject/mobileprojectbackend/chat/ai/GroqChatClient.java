package com.mobileproject.mobileprojectbackend.chat.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/**
 * Client gọi Groq API (OpenAI-compatible) để tạo phản hồi AI cho chat.
 *
 * <p><strong>AI integration:</strong></p>
 * <ul>
 *   <li>Sử dụng model mặc định: {@code llama3-8b-8192}</li>
 *   <li>Endpoint: {@code https://api.groq.com/openai/v1/chat/completions}</li>
 *   <li>Temperature: 0.7 (cân bằng giữa sáng tạo và chính xác)</li>
 *   <li>Timeout: 30s cho request, 10s cho kết nối</li>
 * </ul>
 *
 * <p><strong>Cấu hình:</strong></p>
 * <ul>
 *   <li>{@code groq.api-key} – API key (env: GROQ_API_KEY)</li>
 *   <li>{@code groq.model} – model name (mặc định: llama3-8b-8192)</li>
 * </ul>
 *
 * <p>Nếu API key chưa cấu hình, ném {@link IllegalStateException}.</p>
 */
@Component
public class GroqChatClient {

    private static final URI ENDPOINT = URI.create("https://api.groq.com/openai/v1/chat/completions");

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String apiKey;
    private final String model;

    public GroqChatClient(
            ObjectMapper objectMapper,
            @Value("${groq.api-key:}") String apiKey,
            @Value("${groq.model:llama3-8b-8192}") String model) {
        this.objectMapper = objectMapper;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.model = model == null || model.isBlank() ? "llama3-8b-8192" : model.trim();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Gửi danh sách messages tới Groq API và nhận phản hồi.
     *
     * @param messages danh sách tin nhắn theo format OpenAI (role: system/user/assistant)
     * @return nội dung phản hồi AI
     * @throws IllegalStateException nếu API key chưa cấu hình
     * @throws GroqApiException     nếu API trả về lỗi HTTP
     */
    public String chat(List<Message> messages) throws Exception {
        if (apiKey.isBlank()) {
            throw new IllegalStateException(
                    "Chưa cấu hình Groq API key. Hãy set GROQ_API_KEY (env hoặc secrets.properties) trên backend.");
        }

        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", model);

        ArrayNode array = root.putArray("messages");
        if (messages != null) {
            for (Message message : messages) {
                if (message == null) {
                    continue;
                }
                ObjectNode node = array.addObject();
                node.put("role", message.role());
                node.put("content", message.content());
            }
        }

        root.put("temperature", 0.7);

        String body = objectMapper.writeValueAsString(root);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(ENDPOINT)
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        if (status < 200 || status >= 300) {
            throw new GroqApiException(status, extractErrorMessage(response.body()));
        }

        JsonNode json = objectMapper.readTree(response.body());
        JsonNode choices = json.get("choices");
        if (choices == null || !choices.isArray() || choices.isEmpty()) {
            throw new IllegalStateException("Groq API response thiếu choices");
        }

        String content = choices.get(0)
                .path("message")
                .path("content")
                .asString("");

        return content == null ? "" : content.trim();
    }

    private String extractErrorMessage(String body) {
        if (body == null) {
            return "";
        }

        String trimmed = body.trim();
        if (trimmed.isEmpty()) {
            return "";
        }

        try {
            JsonNode node = objectMapper.readTree(trimmed);
            String message = node.path("error").path("message").asString(null);
            if (message == null || message.isBlank()) {
                message = node.path("message").asString(null);
            }
            if (message != null && !message.isBlank()) {
                return message.trim();
            }
        } catch (Exception ignored) {
            // fall through
        }

        if (trimmed.length() > 260) {
            return trimmed.substring(0, 260) + "...";
        }
        return trimmed;
    }

    /**
     * Exception khi Groq API trả về lỗi HTTP.
     *
     * @param statusCode   mã HTTP (401, 403, 429, 5xx, ...)
     * @param groqMessage  thông báo lỗi chi tiết từ Groq
     */
    public static class GroqApiException extends Exception {
        private final int statusCode;
        private final String groqMessage;

        public GroqApiException(int statusCode, String groqMessage) {
            super("Groq API lỗi HTTP " + statusCode);
            this.statusCode = statusCode;
            this.groqMessage = groqMessage == null ? "" : groqMessage.trim();
        }

        public int statusCode() {
            return statusCode;
        }

        public String groqMessage() {
            return groqMessage;
        }
    }

    /**
     * Record đại diện cho một message trong OpenAI chat format.
     *
     * @param role    vai trò: "system", "user", hoặc "assistant"
     * @param content nội dung tin nhắn
     */
    public record Message(String role, String content) {
    }
}
