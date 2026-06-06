package com.mobileproject.mobileprojectbackend.moment.reaction;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

/**
 * Entity đại diện cho reaction (cảm xúc) của user đối với một kỷ niệm.
 * Ánh xạ tới collection {@code moment_reactions}.
 *
 * <p><strong>Index:</strong></p>
 * <ul>
 *   <li>{@code moment_id} – truy vấn reaction theo moment</li>
 *   <li>{@code id_couple} – truy vấn reaction theo couple</li>
 * </ul>
 *
 * <p>Mỗi user chỉ có thể có tối đa 1 reaction cho mỗi moment.
 * Nếu reaction cùng loại → toggle (xóa), khác loại → cập nhật.</p>
 */
@Document(collection = "moment_reactions")
public class MomentReaction {

    @Id
    private String id;

    /** ID kỷ niệm. */
    @Indexed
    @Field("moment_id")
    private String momentId;

    /** ID cặp đôi. */
    @Indexed
    @Field("id_couple")
    private String coupleId;

    /** ID người thả reaction. */
    @Field("user_id")
    private String userId;

    /** Loại reaction (HEART, FIRE, WOW, LAUGH). */
    private String reaction;

    /** Thời điểm tạo reaction. */
    @Field("created_at")
    private Instant createdAt;

    public MomentReaction() {
    }

    public MomentReaction(String momentId, String coupleId, String userId, String reaction) {
        this.momentId = momentId;
        this.coupleId = coupleId;
        this.userId = userId;
        this.reaction = reaction;
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

    public String getReaction() {
        return reaction;
    }

    public void setReaction(String reaction) {
        this.reaction = reaction;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
