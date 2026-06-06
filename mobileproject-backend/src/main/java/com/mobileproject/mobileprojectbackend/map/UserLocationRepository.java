package com.mobileproject.mobileprojectbackend.map;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * Repository truy vấn collection {@code user_locations}.
 */
public interface UserLocationRepository extends MongoRepository<UserLocation, String> {

    /**
     * Tìm vị trí hiện tại của user trong couple.
     */
    Optional<UserLocation> findByCoupleIdAndUserId(String coupleId, String userId);
}
