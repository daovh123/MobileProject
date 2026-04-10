package com.mobileproject.mobileprojectbackend.notifications;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@Document(collection = "user_fcm_tokens")
public class UserFcmToken {

    @Id
    private String userId;

    private String token;

    private Instant updatedAt;

    public UserFcmToken() {
    }

    public UserFcmToken(String userId, String token, Instant updatedAt) {
        this.userId = userId;
        this.token = token;
        this.updatedAt = updatedAt;
    }
}
