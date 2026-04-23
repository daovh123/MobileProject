package com.mobileproject.mobileprojectbackend.notifications;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserFcmTokenRepository extends MongoRepository<UserFcmToken, String> {
}
