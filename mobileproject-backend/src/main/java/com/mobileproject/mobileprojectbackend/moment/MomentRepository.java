package com.mobileproject.mobileprojectbackend.moment;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MomentRepository extends MongoRepository<Moment, String> {
    List<Moment> findByCoupleIdOrderByCreatedAtDesc(String coupleId);
}
