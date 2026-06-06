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

/**
 * REST controller cho hệ thống thông báo.
 * Base path: {@code /api/notifications}
 *
 * <p>Endpoints:</p>
 * <ul>
 *   <li>POST /fcm-token – đăng ký FCM token</li>
 *   <li>GET  / – lấy danh sách thông báo (phân trang)</li>
 *   <li>GET  /unread-count – đếm thông báo chưa đọc</li>
 *   <li>PUT  /read-all – đánh dấu tất cả đã đọc</li>
 *   <li>PUT  /{id}/read – đánh dấu một thông báo đã đọc</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationController.class);

    private final AuthIdentityService authIdentityService;
    private final UserFcmTokenRepository userFcmTokenRepository;
    private final NotificationService notificationService;

    /** Đăng ký FCM token để nhận push notification. */
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

    /** Lấy danh sách thông báo phân trang (20/trang). */
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

    /** Đếm số thông báo chưa đọc. */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        AuthUser user = authIdentityService.requireCurrentUser(authHeader);
        long count = notificationService.getUnreadCount(user.getId());
        return ResponseEntity.ok(Map.of("count", count));
    }

    /** Đánh dấu tất cả thông báo đã đọc. */
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllRead(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        AuthUser user = authIdentityService.requireCurrentUser(authHeader);
        notificationService.markAllRead(user.getId());
        return ResponseEntity.ok().build();
    }

    /** Đánh dấu một thông báo đã đọc. */
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markRead(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable String id) {
        AuthUser user = authIdentityService.requireCurrentUser(authHeader);
        notificationService.markRead(user.getId(), id);
        return ResponseEntity.ok().build();
    }

    // --- Records ---

    /** Request đăng ký FCM token. */
    public record FcmTokenRequest(String token) {
    }

    /** DTO hiển thị thông tin thông báo. */
    public record NotificationDto(
            String id,
            String type,
            String title,
            String body,
            boolean read,
            Instant createdAt) {
    }

    /** Response phân trang danh sách thông báo. */
    public record NotificationPageResponse(
            List<NotificationDto> content,
            long totalElements,
            int totalPages,
            int currentPage,
            boolean hasNext) {
    }
}
