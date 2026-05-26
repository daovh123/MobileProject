package com.mobileproject.mobileprojectbackend.topup;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface TopUpRequestRepository extends MongoRepository<TopUpRequest, String> {

    Optional<TopUpRequest> findByTransferCode(String transferCode);

    Optional<TopUpRequest> findBySepayId(Long sepayId);

    Optional<TopUpRequest> findByReferenceCode(String referenceCode);
}
