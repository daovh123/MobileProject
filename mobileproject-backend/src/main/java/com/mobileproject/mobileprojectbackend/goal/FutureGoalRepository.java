package com.mobileproject.mobileprojectbackend.goal;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Repository MongoDB cho {@link FutureGoal}.
 * Cung cấp các phương thức truy vấn mục tiêu tương lai theo cặp đôi và trạng thái.
 */
public interface FutureGoalRepository extends MongoRepository<FutureGoal, String> {

    /**
     * Tìm tất cả mục tiêu tương lai của một cặp đôi.
     *
     * @param coupleId ID cặp đôi
     * @return danh sách mục tiêu tương lai
     */
    List<FutureGoal> findByCoupleId(String coupleId);

    /**
     * Tìm mục tiêu tương lai theo cặp đôi và trạng thái.
     *
     * @param coupleId ID cặp đôi
     * @param status   trạng thái mục tiêu {@link GoalStatus}
     * @return danh sách mục tiêu phù hợp
     */
    List<FutureGoal> findByCoupleIdAndStatus(String coupleId, GoalStatus status);
}
