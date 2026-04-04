package com.mobileproject.mobileprojectbackend.history;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserHistoryRepository extends MongoRepository<UserHistory, String> {

    List<UserHistory> findByUserIdOrderByViewedAtDesc(String userId);

    void deleteByUserId(String userId);
}
