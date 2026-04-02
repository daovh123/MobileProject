package com.mobileproject.mobileprojectbackend.auth;

import com.mobileproject.mobileprojectbackend.auth.dto.CoupleCodeResponse;
import com.mobileproject.mobileprojectbackend.auth.dto.CoupleRequestActionResponse;
import com.mobileproject.mobileprojectbackend.auth.dto.CoupleRequestCreateRequest;
import com.mobileproject.mobileprojectbackend.auth.dto.CoupleRequestDecisionRequest;
import com.mobileproject.mobileprojectbackend.auth.dto.CoupleStatusResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;

@Service
public class CoupleService {

    private final AuthIdentityService authIdentityService;
    private final AuthUserRepository authUserRepository;
    private final CoupleRequestRepository coupleRequestRepository;
    private final CoupleCodeCacheService coupleCodeCacheService;

    public CoupleService(AuthIdentityService authIdentityService,
                         AuthUserRepository authUserRepository,
                         CoupleRequestRepository coupleRequestRepository,
                         CoupleCodeCacheService coupleCodeCacheService) {
        this.authIdentityService = authIdentityService;
        this.authUserRepository = authUserRepository;
        this.coupleRequestRepository = coupleRequestRepository;
        this.coupleCodeCacheService = coupleCodeCacheService;
    }

    public CoupleCodeResponse generateMyCode(String authorizationHeader) {
        AuthUser user = authIdentityService.requireCurrentUser(authorizationHeader);
        ensureProfileCompleted(user);
        ensureNotPaired(user);

        CoupleCodeCacheService.CoupleCodeLease lease = coupleCodeCacheService.getOrCreateCode(user.getId());

        return CoupleCodeResponse.success("Couple code is ready", lease.code(), lease.expiresAt().toString());
    }

    public CoupleRequestActionResponse createRequest(String authorizationHeader, CoupleRequestCreateRequest request) {
        AuthUser requester = authIdentityService.requireCurrentUser(authorizationHeader);
        ensureProfileCompleted(requester);
        ensureNotPaired(requester);
        ensureNoPendingRequestForUser(requester.getId(), "You already have a pending couple request");

        String normalizedPartnerCode = normalizeCode(request.partnerCode());
        String recipientUserId = coupleCodeCacheService.findUserIdByCode(normalizedPartnerCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partner code does not exist"));
        AuthUser recipient = authUserRepository.findById(recipientUserId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partner code does not exist"));

        if (requester.getId().equals(recipient.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot connect with your own code");
        }

        ensureProfileCompleted(recipient);
        ensureNotPaired(recipient);
        ensureNoPendingRequestForUser(recipient.getId(), "Partner currently has another pending request");

        String now = Instant.now().toString();

        CoupleRequest coupleRequest = new CoupleRequest();
        coupleRequest.setRequesterUserId(requester.getId());
        coupleRequest.setRequesterUsername(requester.getUsername());
        coupleRequest.setRequesterDisplayName(displayNameOf(requester));
        coupleRequest.setRecipientUserId(recipient.getId());
        coupleRequest.setRecipientUsername(recipient.getUsername());
        coupleRequest.setStatus(CoupleRequestStatus.PENDING);
        coupleRequest.setCreatedAt(now);
        coupleRequest.setUpdatedAt(now);

        CoupleRequest savedRequest = coupleRequestRepository.save(coupleRequest);

        return CoupleRequestActionResponse.success(
                "Couple request sent. Waiting for partner confirmation",
                savedRequest.getId(),
                savedRequest.getStatus().name(),
                savedRequest.getRequesterUsername(),
                savedRequest.getRecipientUsername()
        );
    }

    public CoupleRequestActionResponse decideRequest(String authorizationHeader,
                                                     String requestId,
                                                     CoupleRequestDecisionRequest request) {
        AuthUser recipient = authIdentityService.requireCurrentUser(authorizationHeader);
        ensureProfileCompleted(recipient);

        CoupleRequest coupleRequest = coupleRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Couple request not found"));

        if (!recipient.getId().equals(coupleRequest.getRecipientUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only recipient can decide this request");
        }
        if (coupleRequest.getStatus() != CoupleRequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Couple request is already processed");
        }

        AuthUser requester = authUserRepository.findById(coupleRequest.getRequesterUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Requester no longer exists"));

        String now = Instant.now().toString();
        if (request.accept()) {
            ensureNotPaired(requester);
            ensureNotPaired(recipient);

            requester.setPartnerUserId(recipient.getId());
            recipient.setPartnerUserId(requester.getId());

            authUserRepository.save(requester);
            authUserRepository.save(recipient);
            coupleCodeCacheService.invalidateCodeByUserId(requester.getId());
            coupleCodeCacheService.invalidateCodeByUserId(recipient.getId());

            coupleRequest.setStatus(CoupleRequestStatus.ACCEPTED);
            coupleRequest.setUpdatedAt(now);
            CoupleRequest savedRequest = coupleRequestRepository.save(coupleRequest);

            return CoupleRequestActionResponse.success(
                    "Couple request accepted",
                    savedRequest.getId(),
                    savedRequest.getStatus().name(),
                    savedRequest.getRequesterUsername(),
                    savedRequest.getRecipientUsername()
            );
        }

        coupleRequest.setStatus(CoupleRequestStatus.REJECTED);
        coupleRequest.setUpdatedAt(now);
        CoupleRequest savedRequest = coupleRequestRepository.save(coupleRequest);

        return CoupleRequestActionResponse.success(
                "Couple request rejected",
                savedRequest.getId(),
                savedRequest.getStatus().name(),
                savedRequest.getRequesterUsername(),
                savedRequest.getRecipientUsername()
        );
    }

    public CoupleStatusResponse getStatus(String authorizationHeader) {
        AuthUser user = authIdentityService.requireCurrentUser(authorizationHeader);

        String myCode = null;
        String myCodeExpiresAt = null;
        if (user.isProfileCompleted() && !isPaired(user)) {
            CoupleCodeCacheService.CoupleCodeLease lease = coupleCodeCacheService.getOrCreateCode(user.getId());
            myCode = lease.code();
            myCodeExpiresAt = lease.expiresAt().toString();
        }

        AuthUser partner = null;
        if (isPaired(user)) {
            partner = authUserRepository.findById(user.getPartnerUserId()).orElse(null);
        }

        CoupleRequest incoming = null;
        if (!isPaired(user)) {
            incoming = coupleRequestRepository
                    .findFirstByRecipientUserIdAndStatusOrderByUpdatedAtDesc(user.getId(), CoupleRequestStatus.PENDING)
                    .orElse(null);
        }

        CoupleRequest outgoing = coupleRequestRepository
                .findFirstByRequesterUserIdOrderByUpdatedAtDesc(user.getId())
                .orElse(null);

        return CoupleStatusResponse.success(
                "Couple status fetched successfully",
                user.isProfileCompleted(),
                partner != null,
                partner == null ? null : partner.getUsername(),
                myCode,
                myCodeExpiresAt,
                incoming == null ? null : incoming.getId(),
                incoming == null ? null : incoming.getRequesterUsername(),
                incoming == null ? null : incoming.getRequesterDisplayName(),
                incoming == null ? null : incoming.getCreatedAt(),
                outgoing == null ? null : outgoing.getId(),
                outgoing == null ? null : outgoing.getRecipientUsername(),
                outgoing == null ? null : outgoing.getStatus().name(),
                outgoing == null ? null : outgoing.getUpdatedAt()
        );
    }

    private void ensureProfileCompleted(AuthUser user) {
        if (!user.isProfileCompleted()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please complete profile first"
            );
        }
    }

    private void ensureNotPaired(AuthUser user) {
        if (isPaired(user)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already connected to a partner");
        }
    }

    private void ensureNoPendingRequestForUser(String userId, String conflictMessage) {
        Optional<CoupleRequest> outgoingPending = coupleRequestRepository
                .findFirstByRequesterUserIdAndStatusOrderByUpdatedAtDesc(userId, CoupleRequestStatus.PENDING);
        Optional<CoupleRequest> incomingPending = coupleRequestRepository
                .findFirstByRecipientUserIdAndStatusOrderByUpdatedAtDesc(userId, CoupleRequestStatus.PENDING);

        if (outgoingPending.isPresent() || incomingPending.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, conflictMessage);
        }
    }

    private boolean isPaired(AuthUser user) {
        return !isBlank(user.getPartnerUserId());
    }

    private String displayNameOf(AuthUser user) {
        if (!isBlank(user.getNickName())) {
            return user.getNickName();
        }
        if (!isBlank(user.getFullName())) {
            return user.getFullName();
        }
        return user.getUsername();
    }

    private String normalizeCode(String rawCode) {
        if (rawCode == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Partner code is required");
        }

        String digitsOnly = rawCode.replaceAll("[^0-9]", "");
        if (digitsOnly.length() != 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Partner code must have 6 digits");
        }

        return digitsOnly.substring(0, 3) + "-" + digitsOnly.substring(3);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
