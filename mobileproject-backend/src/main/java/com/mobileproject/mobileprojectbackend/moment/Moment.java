package com.mobileproject.mobileprojectbackend.moment;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

/**
 * Entity đại diện cho một kỷ niệm (moment) của cặp đôi.
 * Ánh xạ tới collection {@code moments} trên MongoDB.
 *
 * <p>Moment bao gồm tiêu đề và hình ảnh, được lưu trữ trên Firebase Storage.
 * Mỗi moment thuộc về một couple (coupleId) và có thể nhận reaction, comment.</p>
 *
 * <p><strong>Index:</strong> Trường {@code coupleId} được index để truy vấn nhanh
 * danh sách kỷ niệm theo cặp đôi.</p>
 */
@Document(collection = "moments")
public class Moment {

    /** ID duy nhất (MongoDB ObjectId). */
    @Id
    private String id;

    /** ID cặp đôi sở hữu kỷ niệm. */
    @Indexed
    @Field("id_couple")
    private String coupleId;

    /** Tiêu đề kỷ niệm. */
    private String title;

    /** URL hình ảnh trên Firebase Storage. */
    @Field("image_url")
    private String imageUrl;

    /** Thời điểm tạo. */
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
