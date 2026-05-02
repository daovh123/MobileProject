package com.mobileproject.mobileprojectbackend.goal;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

    @Document(collection = "saving_goals")
    public class SavingGoal {

        @Id
        private String id;

        @Indexed
        @Field("id_couple")
        private String coupleId;

        private String name;

        private String category;

        @Field("target_amount")
        private Long targetAmount;

        @Field("current_amount")
        private Long currentAmount;

        private Instant deadline;

        private GoalStatus status;

        @Field("created_at")
        private Instant createdAt;

    public SavingGoal() {
    }

    public SavingGoal(String coupleId, String name, String category, Long targetAmount, Instant deadline) {
        this.coupleId = coupleId;
        this.name = name;
        this.category = category;
        this.targetAmount = targetAmount;
        this.currentAmount = 0L;
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

    public Long getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(Long targetAmount) {
        this.targetAmount = targetAmount;
    }

    public Long getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(Long currentAmount) {
        this.currentAmount = currentAmount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Instant getDeadline() {
        return deadline;
    }

    public void setDeadline(Instant deadline) {
        this.deadline = deadline;
    }

    public GoalStatus getStatus() {
        return status;
    }

    public void setStatus(GoalStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}