package com.mobileproject.mobileprojectbackend.goal;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

public abstract class Goal {

    @Id
    private String id;

    @Indexed
    @Field("id_couple")
    private String coupleId;

    private String name;

    private String category;

    private String type;

    private GoalStatus status;

    @Field("deadline")
    private Instant deadline;

    @Field("created_at")
    private Instant createdAt;

    protected Goal() {
    }

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
