package com.mobileproject.mobileprojectbackend.auth;

import com.mobileproject.mobileprojectbackend.auth.dto.CoupleRequestActionResponse;
import com.mobileproject.mobileprojectbackend.auth.dto.CoupleRequestCreateRequest;
import com.mobileproject.mobileprojectbackend.auth.dto.CoupleRequestDecisionRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoupleServiceTest {

    @Mock
    private AuthIdentityService authIdentityService;

    @Mock
    private AuthUserRepository authUserRepository;

    @Mock
    private CoupleRequestRepository coupleRequestRepository;

    @Mock
    private CoupleCodeCacheService coupleCodeCacheService;

    @Mock
    private CoupleInfoRepository coupleInfoRepository;

    @InjectMocks
    private CoupleService coupleService;

    @Test
    void createRequestCreatesPendingRequest() {
        AuthUser requester = user("u1", "alice");
        AuthUser recipient = user("u2", "bob");

        when(authIdentityService.requireCurrentUser("Bearer token-a")).thenReturn(requester);
        when(coupleRequestRepository.findFirstByRequesterUserIdAndStatusOrderByUpdatedAtDesc("u1", CoupleRequestStatus.PENDING))
                .thenReturn(Optional.empty());
        when(coupleRequestRepository.findFirstByRecipientUserIdAndStatusOrderByUpdatedAtDesc("u1", CoupleRequestStatus.PENDING))
                .thenReturn(Optional.empty());
        when(coupleCodeCacheService.findUserIdByCode("123-456")).thenReturn(Optional.of("u2"));
        when(authUserRepository.findById("u2")).thenReturn(Optional.of(recipient));
        when(coupleRequestRepository.findFirstByRequesterUserIdAndStatusOrderByUpdatedAtDesc("u2", CoupleRequestStatus.PENDING))
                .thenReturn(Optional.empty());
        when(coupleRequestRepository.findFirstByRecipientUserIdAndStatusOrderByUpdatedAtDesc("u2", CoupleRequestStatus.PENDING))
                .thenReturn(Optional.empty());
        when(coupleRequestRepository.save(any(CoupleRequest.class))).thenAnswer(invocation -> {
            CoupleRequest request = invocation.getArgument(0);
            request.setId("req-1");
            return request;
        });

        CoupleRequestActionResponse response = coupleService.createRequest(
                "Bearer token-a",
                new CoupleRequestCreateRequest("123456")
        );

        assertTrue(response.success());
        assertEquals("req-1", response.requestId());
        assertEquals("PENDING", response.status());
        assertEquals("alice", response.requesterUsername());
        assertEquals("bob", response.recipientUsername());
    }

    @Test
    void decideRequestAcceptConnectsBothUsers() {
        AuthUser requester = user("u1", "alice");
        AuthUser recipient = user("u2", "bob");

        CoupleRequest request = new CoupleRequest();
        request.setId("req-accept");
        request.setRequesterUserId("u1");
        request.setRequesterUsername("alice");
        request.setRecipientUserId("u2");
        request.setRecipientUsername("bob");
        request.setStatus(CoupleRequestStatus.PENDING);

        when(authIdentityService.requireCurrentUser("Bearer token-b")).thenReturn(recipient);
        when(coupleRequestRepository.findById("req-accept")).thenReturn(Optional.of(request));
        when(authUserRepository.findById("u1")).thenReturn(Optional.of(requester));
        when(coupleRequestRepository.save(any(CoupleRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CoupleRequestActionResponse response = coupleService.decideRequest(
                "Bearer token-b",
                "req-accept",
                new CoupleRequestDecisionRequest(true)
        );

        assertTrue(response.success());
        assertEquals("ACCEPTED", response.status());
        assertEquals("u2", requester.getPartnerUserId());
        assertEquals("u1", recipient.getPartnerUserId());

        verify(authUserRepository).save(requester);
        verify(authUserRepository).save(recipient);
        verify(coupleCodeCacheService).invalidateCodeByUserId("u1");
        verify(coupleCodeCacheService).invalidateCodeByUserId("u2");
        verify(coupleInfoRepository).save(any(CoupleInfo.class));
    }

    @Test
    void decideRequestRejectMarksRequestRejected() {
        AuthUser requester = user("u1", "alice");
        AuthUser recipient = user("u2", "bob");

        CoupleRequest request = new CoupleRequest();
        request.setId("req-reject");
        request.setRequesterUserId("u1");
        request.setRequesterUsername("alice");
        request.setRecipientUserId("u2");
        request.setRecipientUsername("bob");
        request.setStatus(CoupleRequestStatus.PENDING);

        when(authIdentityService.requireCurrentUser("Bearer token-b")).thenReturn(recipient);
        when(coupleRequestRepository.findById("req-reject")).thenReturn(Optional.of(request));
        when(authUserRepository.findById("u1")).thenReturn(Optional.of(requester));
        when(coupleRequestRepository.save(any(CoupleRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CoupleRequestActionResponse response = coupleService.decideRequest(
                "Bearer token-b",
                "req-reject",
                new CoupleRequestDecisionRequest(false)
        );

        assertTrue(response.success());
        assertEquals("REJECTED", response.status());
        assertNotNull(request.getUpdatedAt());
        verify(coupleCodeCacheService, never()).invalidateCodeByUserId(any(String.class));
        verify(coupleInfoRepository, never()).save(any(CoupleInfo.class));
    }

    @Test
    void getStatusReturnsCoupleCodeFromCacheWithExpiry() {
        AuthUser user = user("u9", "zoe");
        Instant expiresAt = Instant.parse("2030-01-01T00:00:00Z");

        when(authIdentityService.requireCurrentUser("Bearer token-c")).thenReturn(user);
        when(coupleCodeCacheService.getOrCreateCode("u9"))
                .thenReturn(new CoupleCodeCacheService.CoupleCodeLease("555-777", expiresAt));

        var response = coupleService.getStatus("Bearer token-c");

        assertTrue(response.success());
        assertEquals("555-777", response.myCoupleCode());
        assertEquals("2030-01-01T00:00:00Z", response.myCoupleCodeExpiresAt());
    }

    private AuthUser user(String id, String username) {
        AuthUser user = new AuthUser(username, username + "@example.com", "hash", "2026-01-01T00:00:00Z");
        user.setId(id);
        user.setProfileCompleted(true);
        return user;
    }
}
