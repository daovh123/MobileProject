package com.mobileproject.mobileprojectbackend.auth;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CoupleRequestRepository extends MongoRepository<CoupleRequest, String> {

    Optional<CoupleRequest> findFirstByRequesterUserIdAndStatusOrderByUpdatedAtDesc(
            String requesterUserId,
            CoupleRequestStatus status
    );

    Optional<CoupleRequest> findFirstByRecipientUserIdAndStatusOrderByUpdatedAtDesc(
            String recipientUserId,
            CoupleRequestStatus status
    );

    Optional<CoupleRequest> findFirstByRequesterUserIdOrderByUpdatedAtDesc(String requesterUserId);

    Optional<CoupleRequest> findFirstByRequesterUserIdAndRecipientUserIdAndStatusOrderByUpdatedAtDesc(
            String requesterUserId,
            String recipientUserId,
            CoupleRequestStatus status
    );

    long deleteByRequesterUsername(String requesterUsername);

    long deleteByRecipientUsername(String recipientUsername);
}
