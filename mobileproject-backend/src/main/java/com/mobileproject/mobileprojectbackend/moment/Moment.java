package com.mobileproject.mobileprojectbackend.moment;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Document(collection = "moments")
public class Moment {

    @Id
    private String id;

    @Indexed
    @Field("id_couple")
    private String coupleId;

    private String title;

    @Field("image_url")
    private String imageUrl;

    @Field("created_at")
    private Instant createdAt;

    public Moment() {
    }

    public Moment(String coupleId, String title, String imageUrl) {
        this.coupleId = coupleId;
        this.title = title;
        this.imageUrl = imageUrl;
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCoupleId() {
        return coupleId;
    }

    public void setCoupleId(String coupleId) {
        this.coupleId = coupleId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
