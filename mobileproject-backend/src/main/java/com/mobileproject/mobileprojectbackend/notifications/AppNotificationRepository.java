package com.mobileproject.mobileprojectbackend.notifications;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository truy vấn collection {@code app_notifications}.
 */
public interface AppNotificationRepository extends MongoRepository<AppNotification, String> {

    /** Tìm thông báo theo user, phân trang, sắp xếp mới nhất trước. */
    Page<AppNotification> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    /** Đếm số thông báo chưa đọc. */
    long countByUserIdAndReadFalse(String userId);

    /** Tìm tất cả thông báo chưa đọc. */
    java.util.List<AppNotification> findByUserIdAndReadFalse(String userId);
}
