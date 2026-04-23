package com.mobileproject.mobileprojectbackend.notifications;

import com.mobileproject.mobileprojectbackend.auth.AuthIdentityService;
import com.mobileproject.mobileprojectbackend.auth.AuthUser;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationController.class);

    private final AuthIdentityService authIdentityService;
    private final UserFcmTokenRepository userFcmTokenRepository;

    @PostMapping("/fcm-token")
    public ResponseEntity<Void> registerFcmToken(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody FcmTokenRequest request
    ) {
        AuthUser user = authIdentityService.requireCurrentUser(authHeader);

        String token = request == null ? null : request.token();
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        userFcmTokenRepository.save(
                new UserFcmToken(
                        user.getId(),
                        token.trim(),
                        Instant.now()
                )
        );

        LOGGER.info("Registered FCM token for user {}", user.getId());

        return ResponseEntity.ok().build();
    }

    public record FcmTokenRequest(String token) {
    }
}
