package com.mobileproject.mobileprojectbackend.goal;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Repository MongoDB cho {@link SavingGoal}.
 * Cung cấp các phương thức truy vấn mục tiêu tiết kiệm theo cặp đôi và trạng thái.
 */
public interface SavingGoalRepository extends MongoRepository<SavingGoal, String> {

    /**
     * Tìm tất cả mục tiêu tiết kiệm của một cặp đôi.
     *
     * @param coupleId ID cặp đôi
     * @return danh sách mục tiêu tiết kiệm
     */
    List<SavingGoal> findByCoupleId(String coupleId);

    /**
     * Tìm mục tiêu tiết kiệm theo cặp đôi và trạng thái.
     *
     * @param coupleId ID cặp đôi
     * @param status   trạng thái mục tiêu {@link GoalStatus}
     * @return danh sách mục tiêu phù hợp
     */
    List<SavingGoal> findByCoupleIdAndStatus(String coupleId, GoalStatus status);
}