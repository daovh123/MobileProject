package com.mobileproject.mobileprojectbackend.moment.comment;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Document(collection = "moment_comments")
public class MomentComment {

    @Id
    private String id;

    @Indexed
    @Field("moment_id")
    private String momentId;

    @Indexed
    @Field("id_couple")
    private String coupleId;

    @Field("user_id")
    private String userId;

    @Field("author_username")
    private String authorUsername;

    @Field("content")
    private String content;

    @Field("created_at")
    private Instant createdAt;

    public MomentComment() {
    }

    public MomentComment(String momentId, String coupleId, String userId, String authorUsername, String content) {
        this.momentId = momentId;
        this.coupleId = coupleId;
        this.userId = userId;
        this.authorUsername = authorUsername;
        this.content = content;
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMomentId() {
        return momentId;
    }

    public void setMomentId(String momentId) {
        this.momentId = momentId;
    }

    public String getCoupleId() {
        return coupleId;
    }

    public void setCoupleId(String coupleId) {
        this.coupleId = coupleId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
