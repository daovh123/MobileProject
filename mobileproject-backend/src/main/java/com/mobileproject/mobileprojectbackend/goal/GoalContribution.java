package com.mobileproject.mobileprojectbackend.goal;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Document(collection = "goal_contributions")
public class GoalContribution {

    @Id
    private String id;

    @Indexed
    @Field("id_goal")
    private String goalId;

    private Long amount;

    @Field("contributor_id")
    private String contributorId;

    private String note;

    private Instant timestamp;

    public GoalContribution() {
    }

    public GoalContribution(String goalId, Long amount, String contributorId, String note) {
        this.goalId = goalId;
        this.amount = amount;
        this.contributorId = contributorId;
        this.note = note;
        this.timestamp = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGoalId() {
        return goalId;
    }

    public void setGoalId(String goalId) {
        this.goalId = goalId;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getContributorId() {
        return contributorId;
    }

    public void setContributorId(String contributorId) {
        this.contributorId = contributorId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}