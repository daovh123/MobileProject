package com.mobileproject.mobileprojectbackend.history;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "user_history")
public class UserHistory {

    @Id
    private String id;

    private String userId;
    private String placeId;
    private Instant viewedAt;

    public UserHistory() {
    }

    public UserHistory(String userId, String placeId) {
        this.userId = userId;
        this.placeId = placeId;
        this.viewedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public Instant getViewedAt() {
        return viewedAt;
    }

    public void setViewedAt(Instant viewedAt) {
        this.viewedAt = viewedAt;
    }
}
