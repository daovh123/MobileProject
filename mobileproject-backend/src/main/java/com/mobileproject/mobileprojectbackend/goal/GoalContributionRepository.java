package com.mobileproject.mobileprojectbackend.goal;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Repository MongoDB cho {@link GoalContribution}.
 * Cung cấp phương thức truy vấn lịch sử đóng góp theo mục tiêu.
 */
public interface GoalContributionRepository extends MongoRepository<GoalContribution, String> {

    /**
     * Tìm tất cả đóng góp cho một mục tiêu, sắp xếp theo thời gian giảm dần.
     *
     * @param goalId ID mục tiêu tiết kiệm
     * @return danh sách đóng góp mới nhất trước
     */
    List<GoalContribution> findByGoalIdOrderByTimestampDesc(String goalId);
}