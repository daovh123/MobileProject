package com.mobileproject.mobileprojectbackend.goal;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface GoalContributionRepository extends MongoRepository<GoalContribution, String> {

    List<GoalContribution> findByGoalIdOrderByTimestampDesc(String goalId);
}