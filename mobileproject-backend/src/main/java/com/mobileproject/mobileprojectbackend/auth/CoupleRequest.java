package com.mobileproject.mobileprojectbackend.auth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "couple_requests")
public class CoupleRequest {

    @Id
    private String id;

    private String requesterUserId;
    private String requesterUsername;
    private String requesterDisplayName;

    private String recipientUserId;
    private String recipientUsername;

    private CoupleRequestStatus status;
    private String createdAt;
    private String updatedAt;
}
