package com.mobileproject.mobileprojectbackend.moment;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface MomentRepository extends MongoRepository<Moment, String> {
    List<Moment> findByCoupleIdOrderByCreatedAtDesc(String coupleId);
}
