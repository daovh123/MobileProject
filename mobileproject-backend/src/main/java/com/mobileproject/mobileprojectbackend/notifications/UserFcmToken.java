package com.mobileproject.mobileprojectbackend.notifications;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Entity lưu FCM token của user để gửi push notification.
 * Ánh xạ tới collection {@code user_fcm_tokens}.
 *
 * <p>Document ID chính là userId (1 user = 1 token).
 * Token được cập nhật mỗi khi user mở app (client gửi token mới).</p>
 */
@Getter
@Setter
@Document(collection = "user_fcm_tokens")
public class UserFcmToken {

    /** ID người dùng (cũng là document ID). */
    @Id
    private String userId;

    /** FCM registration token. */
    private String token;

    /** Thời điểm cập nhật token. */
    private Instant updatedAt;

    public UserFcmToken() {
    }

    public UserFcmToken(String userId, String token, Instant updatedAt) {
        this.userId = userId;
        this.token = token;
        this.updatedAt = updatedAt;
    }
}