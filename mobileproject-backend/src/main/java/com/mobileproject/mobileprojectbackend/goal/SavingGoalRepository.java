package com.mobileproject.mobileprojectbackend.goal;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SavingGoalRepository extends MongoRepository<SavingGoal, String> {

    List<SavingGoal> findByCoupleId(String coupleId);

    List<SavingGoal> findByCoupleIdAndStatus(String coupleId, GoalStatus status);
}