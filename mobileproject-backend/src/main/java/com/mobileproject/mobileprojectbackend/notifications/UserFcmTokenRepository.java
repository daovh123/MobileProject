package com.mobileproject.mobileprojectbackend.notifications;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository truy vấn collection {@code user_fcm_tokens}.
 * Document ID = userId, nên dùng findById(userId) để tìm token.
 */
public interface UserFcmTokenRepository extends MongoRepository<UserFcmToken, String> {
}
