package com.mobileproject.mobileprojectbackend.notifications;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AppNotificationRepository extends MongoRepository<AppNotification, String> {

    Page<AppNotification> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    long countByUserIdAndReadFalse(String userId);

    java.util.List<AppNotification> findByUserIdAndReadFalse(String userId);
}
