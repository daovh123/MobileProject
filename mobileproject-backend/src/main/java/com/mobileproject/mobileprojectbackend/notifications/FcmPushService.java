package com.mobileproject.mobileprojectbackend.notifications;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

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

    private volatile boolean missingFirebaseConfigLogged = false;
    private volatile boolean pathReadFailureLogged = false;

    public FcmPushService(
            UserFcmTokenRepository userFcmTokenRepository,
            @Value("${firebase.credentials.path:}") String credentialsPath
    ) {
        this.userFcmTokenRepository = userFcmTokenRepository;
        this.credentialsPath = credentialsPath;
    }

    @PostConstruct
    void logFirebaseConfigStatus() {
        String path = credentialsPath == null ? "" : credentialsPath.trim();
        if (path.isBlank()) {
            if (hasClasspathCredentials()) {
                LOGGER.info("Firebase credentials path is empty; will use classpath resource firebase-service-account.json");
            } else {
                LOGGER.warn("Firebase credentials path is empty. Set FIREBASE_CREDENTIALS_PATH to enable FCM push.");
            }
            return;
        }

        boolean exists = false;
        if (!path.startsWith("classpath:")) {
            exists = Files.exists(Path.of(path));
        }
        if (exists) {
            LOGGER.warn("Firebase credentials file not found at {}", path);
        } else {
            LOGGER.info("Firebase credentials loaded from {}", path);
        }
    }

    public void sendChatMessagePush(
            String receiverUserId,
            String coupleId,
            String senderUsername,
            String text,
            String messageId,
            Instant createdAt
    ) {
        if (receiverUserId == null || receiverUserId.isBlank()) {
            return;
        }

        UserFcmToken tokenEntity = userFcmTokenRepository.findById(receiverUserId).orElse(null);
        String token = tokenEntity == null ? null : tokenEntity.getToken();
        if (token == null || token.isBlank()) {
            LOGGER.debug("Skip FCM push: user {} has no registered token", receiverUserId);
            return;
        }

        String tokenPreview = token.length() <= 12
            ? token
            : token.substring(0, 6) + "..." + token.substring(token.length() - 6);

        LOGGER.info(
            "Attempting FCM push to user {} (token={}) for message {}",
            receiverUserId,
            tokenPreview,
            messageId == null ? "" : messageId
        );

        FirebaseMessaging messaging = firebaseMessagingOrNull();
        if (messaging == null) {
            if (!missingFirebaseConfigLogged) {
                missingFirebaseConfigLogged = true;
                LOGGER.warn("Skip FCM push: Firebase Admin is not initialized. Configure FIREBASE_CREDENTIALS_PATH or application default credentials.");
            }
            return;
        }

        String safeText = text == null ? "" : text;
        String safeSender = senderUsername == null ? "Partner" : senderUsername;
        String safeCreatedAt = createdAt != null ? createdAt.toString() : Instant.now().toString();
        String notificationTitle = safeSender.isBlank() ? "Tin nhan moi" : safeSender;
        String notificationBody = safeText.isBlank() ? "Ban co tin nhan moi" : safeText;

        Message message = Message.builder()
                .setToken(token.trim())
                .putData("type", "chat_message")
                .putData("coupleId", coupleId == null ? "" : coupleId)
                .putData("senderUsername", safeSender)
                .putData("text", safeText)
                .putData("messageId", messageId == null ? "" : messageId)
                .putData("createdAt", safeCreatedAt)
            .setNotification(
                Notification.builder()
                    .setTitle(notificationTitle)
                    .setBody(notificationBody)
                    .build()
            )
                .setAndroidConfig(
                        AndroidConfig.builder()
                                .setPriority(AndroidConfig.Priority.HIGH)
                    .setNotification(
                        AndroidNotification.builder()
                            .setChannelId(CHAT_CHANNEL_ID)
                            .setSound("default")
                            .build()
                    )
                                .build()
                )
                .build();

        try {
            String fcmMessageId = messaging.send(message);
            missingFirebaseConfigLogged = false;
            LOGGER.info("FCM push sent to user {} with firebaseMessageId={}", receiverUserId, fcmMessageId);
        } catch (Exception ex) {
            String errorText = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
            if (errorText.contains("registration-token") || errorText.contains("not registered")) {
                LOGGER.info("Removing stale FCM token for user {}", receiverUserId);
                userFcmTokenRepository.deleteById(receiverUserId);
            }
            LOGGER.warn("Failed to send FCM chat push to user {}", receiverUserId, ex);
        }
    }

    private FirebaseMessaging firebaseMessagingOrNull() {
        try {
            ensureInitialized();
        } catch (Exception ex) {
            LOGGER.debug("Firebase initialization failed", ex);
            return null;
        }

        if (FirebaseApp.getApps().isEmpty()) {
            return null;
        }

        try {
            return FirebaseMessaging.getInstance();
        } catch (Exception ex) {
            LOGGER.debug("FirebaseMessaging not available", ex);
            return null;
        }
    }

    private void ensureInitialized() throws Exception {
        if (firebaseInitialized || !FirebaseApp.getApps().isEmpty()) {
            firebaseInitialized = true;
            return;
        }

        long now = System.currentTimeMillis();
        if ((now - lastInitAttemptEpochMs) < INIT_RETRY_INTERVAL_MS) {
            return;
        }

        synchronized (initLock) {
            if (firebaseInitialized || !FirebaseApp.getApps().isEmpty()) {
                firebaseInitialized = true;
                return;
            }

            long current = System.currentTimeMillis();
            if ((current - lastInitAttemptEpochMs) < INIT_RETRY_INTERVAL_MS) {
                return;
            }
            lastInitAttemptEpochMs = current;

            GoogleCredentials credentials = loadCredentialsOrNull();
            if (credentials == null) {
                LOGGER.info("Skipping FCM push: Firebase credentials not configured. Set FIREBASE_CREDENTIALS_PATH or firebase.credentials.path");
                return;
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();

            FirebaseApp.initializeApp(options);
            firebaseInitialized = true;
            missingFirebaseConfigLogged = false;
            LOGGER.info("Firebase Admin initialized successfully for FCM push");
        }
    }

    private GoogleCredentials loadCredentialsOrNull() {
        String path = credentialsPath == null ? "" : credentialsPath.trim();
        if (!path.isBlank()) {
            try (InputStream stream = Files.newInputStream(Path.of(path))) {
                pathReadFailureLogged = false;
                return GoogleCredentials.fromStream(stream);
            } catch (Exception ex) {
                if (!pathReadFailureLogged) {
                    pathReadFailureLogged = true;
                    LOGGER.warn("Failed to read Firebase credentials from firebase.credentials.path={}. Trying fallback sources.", path);
                }
            }
        }

        try (InputStream stream = FcmPushService.class.getClassLoader().getResourceAsStream("firebase-service-account.json")) {
            if (stream != null) {
                LOGGER.info("Using Firebase credentials from classpath resource firebase-service-account.json");
                return GoogleCredentials.fromStream(stream);
            }
        } catch (Exception ex) {
            LOGGER.info("Failed to read Firebase credentials from classpath resource", ex);
            return null;
        }

        try {
            return GoogleCredentials.getApplicationDefault();
        } catch (Exception ex) {
            return null;
        }
    }

    private boolean hasClasspathCredentials() {
        try (InputStream stream = FcmPushService.class.getClassLoader().getResourceAsStream("firebase-service-account.json")) {
            return stream != null;
        } catch (Exception ex) {
            return false;
        }
    }
}
