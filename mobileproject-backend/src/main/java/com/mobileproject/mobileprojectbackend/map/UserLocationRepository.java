package com.mobileproject.mobileprojectbackend.map;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserLocationRepository extends MongoRepository<UserLocation, String> {

    Optional<UserLocation> findByCoupleIdAndUserId(String coupleId, String userId);
}
