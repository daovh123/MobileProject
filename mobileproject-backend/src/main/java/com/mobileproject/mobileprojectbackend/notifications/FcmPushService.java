package com.mobileproject.mobileprojectbackend.notifications;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.*;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

/**
 * Service gửi push notification qua Firebase Cloud Messaging (FCM).
 *
 * <p><strong>FCM integration:</strong></p>
 * <ul>
 *   <li>Khởi động lazy: Firebase Admin SDK được init lần đầu khi cần gửi push</li>
 *   <li>Hỗ trợ credentials từ: file path, classpath resource, Application Default Credentials</li>
 *   <li>Tự động xóa token không hợp lệ (UNREGISTERED) khi gửi thất bại</li>
 *   <li>Retry init cách nhau 15s nếu lần trước thất bại</li>
 * </ul>
 *
 * <p><strong>Cấu hình:</strong></p>
 * <ul>
 *   <li>{@code firebase.credentials.path} – đường dẫn service account JSON
 *       (hỗ trợ classpath: và absolute path)</li>
 *   <li>{@code firebase.storage.bucket} – tên Firebase Storage bucket</li>
 * </ul>
 *
 * <p><strong>Loại push:</strong></p>
 * <ul>
 *   <li>{@link #sendChatMessagePush} – push tin nhắn chat (data-only, channelId=chat_messages)</li>
 *   <li>{@link #sendGeneralPush} – push thông báo chung (kèm notification payload)</li>
 * </ul>
 */
@Service
public class FcmPushService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FcmPushService.class);
    private static final long INIT_RETRY_INTERVAL_MS = 15_000L;
    private static final String CHAT_CHANNEL_ID = "chat_messages";

    private final UserFcmTokenRepository userFcmTokenRepository;
    private final String credentialsPath;

    private final Object initLock = new Object();
    private volatile boolean firebaseInitialized = false;
    private volatile long lastInitAttemptEpochMs = 0L;
    private volatile boolean pathReadFailureLogged = false;

    public FcmPushService(
            UserFcmTokenRepository userFcmTokenRepository,
            @Value("${firebase.credentials.path:}") String credentialsPath) {
        this.userFcmTokenRepository = userFcmTokenRepository;
        this.credentialsPath = credentialsPath;
    }

    /**
     * Log trạng thái cấu hình Firebase khi khởi tạo bean.
     */
    @PostConstruct
    void logFirebaseConfigStatus() {
        String path = credentialsPath == null ? "" : credentialsPath.trim();
        if (path.isBlank()) {
            if (hasClasspathCredentials()) {
                LOGGER.info(
                        "Firebase credentials path is empty; will use classpath resource firebase-service-account.json");
            } else {
                LOGGER.warn("Firebase credentials path is empty. Set FIREBASE_CREDENTIALS_PATH to enable FCM push.");
            }
            return;
        }

        // Xử lý logic check tồn tại file cho cả classpath và file system
        if (path.startsWith("classpath:")) {
            String classpathResource = path.substring("classpath:".length()).trim();
            if (!hasClasspathResource(classpathResource)) {
                LOGGER.warn("Firebase classpath resource not found at {}", path);
            } else {
                LOGGER.info("Firebase credentials found in {}", path);
            }
        } else {
            if (!Files.exists(Path.of(path))) {
                LOGGER.warn("Firebase credentials file not found at {}", path);
            } else {
                LOGGER.info("Firebase credentials found at {}", path);
            }
        }
    }

    /**
     * Gửi FCM push cho tin nhắn chat (data-only message).
     * Client quyết định cách hiển thị (foreground/background).
     *
     * @param receiverUserId ID người nhận
     * @param coupleId       ID couple
     * @param senderUsername tên người gửi
     * @param text           nội dung tin nhắn
     * @param messageId      ID tin nhắn
     * @param createdAt      thời điểm gửi
     */
    public void sendChatMessagePush(String receiverUserId, String coupleId, String senderUsername, String text,
            String messageId, Instant createdAt) {
        if (receiverUserId == null || receiverUserId.isBlank()) {
            return;
        }

        String normalizedText = text == null ? "" : text.trim();
        if (normalizedText.isBlank()) {
            return;
        }

        String normalizedReceiverUserId = receiverUserId.trim();
        UserFcmToken userToken = userFcmTokenRepository.findById(normalizedReceiverUserId).orElse(null);
        if (userToken == null || userToken.getToken() == null || userToken.getToken().isBlank()) {
            return;
        }

        FirebaseMessaging firebaseMessaging = firebaseMessagingOrNull();
        if (firebaseMessaging == null) {
            return;
        }

        String safeSenderUsername = senderUsername == null || senderUsername.isBlank()
                ? "Partner"
                : senderUsername.trim();
        String safeCoupleId = coupleId == null ? "" : coupleId.trim();
        String safeMessageId = messageId == null ? "" : messageId.trim();
        String safeCreatedAt = (createdAt == null ? Instant.now() : createdAt).toString();

        Message message = Message.builder()
                .setToken(userToken.getToken().trim())
                .putData("type", "chat_message")
                .putData("channelId", CHAT_CHANNEL_ID)
                .putData("coupleId", safeCoupleId)
                .putData("senderUsername", safeSenderUsername)
                .putData("conversationTitle", safeSenderUsername)
                .putData("text", normalizedText)
                .putData("body", normalizedText)
                .putData("messageId", safeMessageId)
                .putData("createdAt", safeCreatedAt)
                .build();

        try {
            String responseId = firebaseMessaging.send(message);
            LOGGER.debug(
                    "Sent FCM chat push to user={} coupleId={} responseId={}",
                    normalizedReceiverUserId,
                    safeCoupleId,
                    responseId);
        } catch (FirebaseMessagingException exception) {
            if (isInvalidToken(exception)) {
                userFcmTokenRepository.deleteById(normalizedReceiverUserId);
                LOGGER.info("Removed invalid FCM token for user={}", normalizedReceiverUserId);
                return;
            }
            LOGGER.warn("Failed to send FCM chat push to user={}: {}", normalizedReceiverUserId,
                    exception.getMessage());
        } catch (Exception exception) {
            LOGGER.warn("Unexpected error while sending FCM chat push to user={}", normalizedReceiverUserId, exception);
        }
    }

    /**
     * Gửi FCM push thông báo chung (kèm notification payload để hiển thị trên thanh thông báo).
     *
     * @param receiverUserId ID người nhận
     * @param type           loại thông báo
     * @param title          tiêu đề
     * @param body           nội dung
     */
    public void sendGeneralPush(String receiverUserId, String type, String title, String body) {
        if (receiverUserId == null || receiverUserId.isBlank()) return;

        String normalizedUserId = receiverUserId.trim();
        UserFcmToken userToken = userFcmTokenRepository.findById(normalizedUserId).orElse(null);
        if (userToken == null || userToken.getToken() == null || userToken.getToken().isBlank()) return;

        FirebaseMessaging firebaseMessaging = firebaseMessagingOrNull();
        if (firebaseMessaging == null) return;

        String safeType = type == null ? "general" : type.trim();
        String safeTitle = title == null ? "" : title.trim();
        String safeBody = body == null ? "" : body.trim();

        Message message = Message.builder()
                .setToken(userToken.getToken().trim())
                .putData("type", safeType)
                .putData("title", safeTitle)
                .putData("body", safeBody)
                .setNotification(
                        com.google.firebase.messaging.Notification.builder()
                                .setTitle(safeTitle)
                                .setBody(safeBody)
                                .build()
                )
                .build();

        try {
            String responseId = firebaseMessaging.send(message);
            LOGGER.debug("Sent FCM general push to user={} type={} responseId={}", normalizedUserId, safeType, responseId);
        } catch (FirebaseMessagingException exception) {
            if (isInvalidToken(exception)) {
                userFcmTokenRepository.deleteById(normalizedUserId);
                LOGGER.info("Removed invalid FCM token for user={}", normalizedUserId);
                return;
            }
            LOGGER.warn("Failed to send FCM general push to user={}: {}", normalizedUserId, exception.getMessage());
        } catch (Exception exception) {
            LOGGER.warn("Unexpected error while sending FCM general push to user={}", normalizedUserId, exception);
        }
    }

    private FirebaseMessaging firebaseMessagingOrNull() {
        try {
            ensureInitialized();
        } catch (Exception ex) {
            return null;
        }
        if (FirebaseApp.getApps().isEmpty())
            return null;
        try {
            return FirebaseMessaging.getInstance();
        } catch (Exception ex) {
            return null;
        }
    }

    private boolean isInvalidToken(FirebaseMessagingException exception) {
        if (exception == null) {
            return false;
        }
        MessagingErrorCode errorCode = exception.getMessagingErrorCode();
        return MessagingErrorCode.UNREGISTERED.equals(errorCode);
    }

    private void ensureInitialized() throws Exception {
        if (firebaseInitialized || !FirebaseApp.getApps().isEmpty()) {
            firebaseInitialized = true;
            return;
        }
        long now = System.currentTimeMillis();
        if ((now - lastInitAttemptEpochMs) < INIT_RETRY_INTERVAL_MS)
            return;

        synchronized (initLock) {
            if (firebaseInitialized || !FirebaseApp.getApps().isEmpty()) {
                firebaseInitialized = true;
                return;
            }
            lastInitAttemptEpochMs = System.currentTimeMillis();

            GoogleCredentials credentials = loadCredentialsOrNull();
            if (credentials == null)
                return;

            FirebaseOptions options = FirebaseOptions.builder().setCredentials(credentials).build();
            FirebaseApp.initializeApp(options);
            firebaseInitialized = true;
            LOGGER.info("Firebase Admin initialized successfully");
        }
    }

    private GoogleCredentials loadCredentialsOrNull() {
        String path = credentialsPath == null ? "" : credentialsPath.trim();

        if (!path.isBlank()) {
            // MERGED LOGIC: Hỗ trợ cả classpath: và path trực tiếp
            if (path.startsWith("classpath:")) {
                String resource = normalizeClasspathResource(path.substring(10).trim());
                try (InputStream stream = FcmPushService.class.getClassLoader().getResourceAsStream(resource)) {
                    if (stream != null) {
                        pathReadFailureLogged = false;
                        return GoogleCredentials.fromStream(stream);
                    }
                } catch (Exception ex) {
                    LOGGER.warn("Failed to read Firebase classpath resource: {}", path);
                }
            } else {
                try (InputStream stream = Files.newInputStream(Path.of(path))) {
                    pathReadFailureLogged = false;
                    return GoogleCredentials.fromStream(stream);
                } catch (Exception ex) {
                    if (!pathReadFailureLogged) {
                        pathReadFailureLogged = true;
                        LOGGER.warn("Failed to read Firebase file from path={}", path);
                    }
                }
            }
        }

        // Fallback 1: Default classpath file
        try (InputStream stream = FcmPushService.class.getClassLoader()
                .getResourceAsStream("firebase-service-account.json")) {
            if (stream != null)
                return GoogleCredentials.fromStream(stream);
        } catch (Exception ignored) {
        }

        // Fallback 2: Application Default Credentials
        try {
            return GoogleCredentials.getApplicationDefault();
        } catch (Exception ex) {
            return null;
        }
    }

    // Helper methods từ nhánh feature/UI-advance
    private boolean hasClasspathCredentials() {
        return hasClasspathResource("firebase-service-account.json");
    }

    private boolean hasClasspathResource(String classpathResource) {
        String normalized = normalizeClasspathResource(classpathResource);
        try (InputStream stream = FcmPushService.class.getClassLoader().getResourceAsStream(normalized)) {
            return stream != null;
        } catch (Exception ex) {
            return false;
        }
    }

    private String normalizeClasspathResource(String classpathResource) {
        if (classpathResource == null || classpathResource.isBlank()) {
            return "firebase-service-account.json";
        }
        String normalized = classpathResource.replace("\\", "/").trim();
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }
}