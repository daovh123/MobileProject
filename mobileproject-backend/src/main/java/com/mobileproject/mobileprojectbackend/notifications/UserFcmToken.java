package com.mobileproject.mobileprojectbackend.notifications;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "user_fcm_tokens")
public class UserFcmToken {

    @Id
    private String id;

    private String token;

    public UserFcmToken() {
    }

    public UserFcmToken(String id, String token) {
        this.id = id;
        this.token = token;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}