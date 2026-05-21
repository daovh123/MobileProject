package com.mobileproject.mobileprojectbackend.notifications;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@Document(collection = "app_notifications")
@CompoundIndex(name = "user_created", def = "{'userId': 1, 'createdAt': -1}")
public class AppNotification {

    @Id
    private String id;

    @Indexed
    private String userId;

    private NotificationType type;

    private String title;

    private String body;

    private boolean read;

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
