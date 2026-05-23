package com.mobileproject.mobileprojectbackend.notifications;

import com.mobileproject.mobileprojectbackend.auth.AuthIdentityService;
import com.mobileproject.mobileprojectbackend.auth.AuthUser;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationController.class);

    private final AuthIdentityService authIdentityService;
    private final UserFcmTokenRepository userFcmTokenRepository;
    private final NotificationService notificationService;

    @PostMapping("/fcm-token")
    public ResponseEntity<Void> registerFcmToken(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody FcmTokenRequest request) {
        AuthUser user = authIdentityService.requireCurrentUser(authHeader);
        String token = request == null ? null : request.token();
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        userFcmTokenRepository.save(new UserFcmToken(user.getId(), token.trim(), Instant.now()));
        LOGGER.info("Registered FCM token for user {}", user.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<NotificationPageResponse> getNotifications(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestParam(defaultValue = "0") int page) {
        AuthUser user = authIdentityService.requireCurrentUser(authHeader);
        Page<AppNotification> result = notificationService.getNotifications(user.getId(), page);
        List<NotificationDto> dtos = result.getContent().stream()
                .map(n -> new NotificationDto(
                        n.getId(),
                        n.getType() != null ? n.getType().name() : "GENERAL",
                        n.getTitle(),
                        n.getBody(),
                        n.isRead(),
                        n.getCreatedAt()))
                .toList();
        return ResponseEntity.ok(new NotificationPageResponse(
                dtos,
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber(),
                result.hasNext()));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        AuthUser user = authIdentityService.requireCurrentUser(authHeader);
        long count = notificationService.getUnreadCount(user.getId());
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllRead(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        AuthUser user = authIdentityService.requireCurrentUser(authHeader);
        notificationService.markAllRead(user.getId());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markRead(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable String id) {
        AuthUser user = authIdentityService.requireCurrentUser(authHeader);
        notificationService.markRead(user.getId(), id);
        return ResponseEntity.ok().build();
    }

    // --- Records ---

    public record FcmTokenRequest(String token) {
    }

    public record NotificationDto(
            String id,
            String type,
            String title,
            String body,
            boolean read,
            Instant createdAt) {
    }

    public record NotificationPageResponse(
            List<NotificationDto> content,
            long totalElements,
            int totalPages,
            int currentPage,
            boolean hasNext) {
    }
}
