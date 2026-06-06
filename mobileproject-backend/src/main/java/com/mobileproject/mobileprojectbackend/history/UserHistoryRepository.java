package com.mobileproject.mobileprojectbackend.history;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Repository truy vấn collection {@code user_history}.
 */
public interface UserHistoryRepository extends MongoRepository<UserHistory, String> {

    /** Tìm lịch sử xem của user, sắp xếp mới nhất trước. */
    List<UserHistory> findByUserIdOrderByViewedAtDesc(String userId);

    /** Xóa toàn bộ lịch sử xem của user. */
    void deleteByUserId(String userId);
}
