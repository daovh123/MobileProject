package com.mobileproject.mobileprojectbackend.notifications;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Entity đại diện cho một thông báo trong ứng dụng.
 * Ánh xạ tới collection {@code app_notifications}.
 *
 * <p><strong>Index:</strong></p>
 * <ul>
 *   <li>{@code userId} – index đơn để query thông báo theo user</li>
 *   <li>Compound index {@code userId + createdAt (desc)} – tối ưu phân trang</li>
 * </ul>
 */
@Getter
@Setter
@Document(collection = "app_notifications")
@CompoundIndex(name = "user_created", def = "{'userId': 1, 'createdAt': -1}")
public class AppNotification {

    @Id
    private String id;

    /** ID người nhận thông báo. */
    @Indexed
    private String userId;

    /** Loại thông báo. */
    private NotificationType type;

    /** Tiêu đề thông báo. */
    private String title;

    /** Nội dung thông báo. */
    private String body;

    /** Đã đọc hay chưa. */
    private boolean read;

    /** Thời điểm tạo. */
    private Instant createdAt;

    public AppNotification() {
    }

    public AppNotification(String userId, NotificationType type, String title, String body) {
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.body = body;
        this.read = false;
        this.createdAt = Instant.now();
    }
}
