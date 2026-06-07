package com.mobileproject.mobileprojectbackend.goal;

import java.time.Instant;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "saving_goals")
public class SavingGoal extends Goal {

    @Field("target_amount")
    private Long targetAmount;

    @Field("current_amount")
    private Long currentAmount;

    @Field("withdrawn_amount")
    private Long withdrawnAmount;

    public SavingGoal() {
        super();
    }

    public SavingGoal(String coupleId, String name, String category, Long targetAmount, Instant deadline) {
        super(coupleId, name, category, GoalType.SAVING, deadline);
        this.targetAmount = targetAmount;
        this.currentAmount = 0L;
        this.withdrawnAmount = 0L;
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

    public Long getWithdrawnAmount() {
        return withdrawnAmount;
    }

    public void setWithdrawnAmount(Long withdrawnAmount) {
        this.withdrawnAmount = withdrawnAmount;
    }
}
