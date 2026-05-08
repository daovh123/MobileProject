package com.mobileproject.mobileprojectbackend.goal;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FutureGoalRepository extends MongoRepository<FutureGoal, String> {

    List<FutureGoal> findByCoupleId(String coupleId);

    List<FutureGoal> findByCoupleIdAndStatus(String coupleId, GoalStatus status);
}
