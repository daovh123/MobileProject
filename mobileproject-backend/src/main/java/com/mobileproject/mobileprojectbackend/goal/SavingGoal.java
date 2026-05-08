package com.mobileproject.mobileprojectbackend.goal;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Document(collection = "saving_goals")
public class SavingGoal extends Goal {

    @Field("target_amount")
    private Long targetAmount;

    @Field("current_amount")
    private Long currentAmount;

    public SavingGoal() {
        super();
    }

    public SavingGoal(String coupleId, String name, String category, Long targetAmount, Instant deadline) {
        super(coupleId, name, category, GoalType.SAVING, deadline);
        this.targetAmount = targetAmount;
        this.currentAmount = 0L;
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
}