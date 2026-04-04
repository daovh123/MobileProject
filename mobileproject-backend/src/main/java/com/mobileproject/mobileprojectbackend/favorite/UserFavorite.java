package com.mobileproject.mobileprojectbackend.favorite;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "user_favorites")
@CompoundIndex(name = "user_place_idx", def = "{'userId': 1, 'placeId': 1}", unique = true)
public class UserFavorite {

    @Id
    private String id;

    private String userId;
    private String placeId;
    private Instant createdAt;

    public UserFavorite() {
    }

    public UserFavorite(String userId, String placeId) {
        this.userId = userId;
        this.placeId = placeId;
        this.createdAt = Instant.now();
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
