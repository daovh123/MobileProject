package com.mobileproject.mobileprojectbackend.goal;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

/**
 * Entity trừu tượng cơ sở cho tất cả các loại mục tiêu (Goal) của cặp đôi.
 *
 * <p>Là lớp cha của {@link SavingGoal} (mục tiêu tiết kiệm) và {@link FutureGoal} (mục tiêu tương lai).
 * Không ánh xạ trực tiếp tới collection riêng – mỗi subclass có collection riêng.</p>
 *
 * <h3>Indexes:</h3>
 * <ul>
 *   <li>{@code id_couple} – indexed, truy vấn nhanh theo cặp đôi</li>
 * </ul>
 *
 * <h3>Mối quan hệ:</h3>
 * <ul>
 *   <li>{@code coupleId} → tham chiếu đến {@code CoupleInfo.id}</li>
 * </ul>
 */
public abstract class Goal {

    /** ID duy nhất của mục tiêu (MongoDB ObjectId). */
    @Id
    private String id;

    /** ID cặp đôi sở hữu mục tiêu. Được index. */
    @Indexed
    @Field("id_couple")
    private String coupleId;

    /** Tên mục tiêu. */
    private String name;

    /** Danh mục mục tiêu. */
    private String category;

    /** Loại mục tiêu (lưu dạng String: "SAVING" hoặc "FUTURE"). */
    private String type;

    /** Trạng thái hiện tại của mục tiêu. */
    private GoalStatus status;

    /** Thời hạn hoàn thành mục tiêu. */
    @Field("deadline")
    private Instant deadline;

    /** Thời điểm tạo mục tiêu. */
    @Field("created_at")
    private Instant createdAt;

    /**
     * Constructor mặc định bảo vệ cho subclass.
     */
    protected Goal() {
    }

    /**
     * Tạo mục tiêu mới với trạng thái mặc định là {@code IN_PROGRESS}.
     *
     * @param coupleId ID cặp đôi
     * @param name     tên mục tiêu
     * @param category danh mục
     * @param type     loại mục tiêu {@link GoalType}
     * @param deadline thời hạn
     */
    protected Goal(String coupleId, String name, String category, GoalType type, Instant deadline) {
        this.coupleId = coupleId;
        this.name = name;
        this.category = category;
        this.type = type != null ? type.name() : GoalType.SAVING.name();
        this.deadline = deadline;
        this.status = GoalStatus.IN_PROGRESS;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Lấy loại mục tiêu dưới dạng String, có fallback về {@code "SAVING"} nếu giá trị
     * trong DB không hợp lệ (hỗ trợ legacy data).
     *
     * @return loại mục tiêu hợp lệ
     */
    public String getType() {
        try {
            if (type != null) {
                GoalType.valueOf(type);
                return type;
            }
        } catch (Exception e) {
            // Ignore invalid DB string and fallback to SAVING
        }
        return GoalType.SAVING.name();
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * Lấy loại mục tiêu dưới dạng enum {@link GoalType}, fallback về {@code SAVING}
     * nếu giá trị trong DB không hợp lệ.
     *
     * @return {@link GoalType} enum
     */
    public GoalType getGoalTypeEnum() {
        try {
            return type != null ? GoalType.valueOf(type) : GoalType.SAVING;
        } catch (Exception e) {
            return GoalType.SAVING;
        }
    }

    public GoalStatus getStatus() {
        return status;
    }

    public void setStatus(GoalStatus status) {
        this.status = status;
    }

    public Instant getDeadline() {
        return deadline;
    }

    public void setDeadline(Instant deadline) {
        this.deadline = deadline;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
