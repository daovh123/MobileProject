package com.mobileproject.mobileprojectbackend.payout;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PayoutRequestRepository extends MongoRepository<PayoutRequest, String> {
    Optional<PayoutRequest> findByTransferCode(String transferCode);
    Optional<PayoutRequest> findBySepayId(Long sepayId);
    Optional<PayoutRequest> findByReferenceCode(String referenceCode);
}

