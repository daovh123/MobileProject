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
        private AuthUserCacheService authUserCacheService;

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
                when(coupleRequestRepository.findFirstByRequesterUserIdAndStatusOrderByUpdatedAtDesc("u1",
                                CoupleRequestStatus.PENDING))
                                .thenReturn(Optional.empty());
                when(coupleRequestRepository.findFirstByRecipientUserIdAndStatusOrderByUpdatedAtDesc("u1",
                                CoupleRequestStatus.PENDING))
                                .thenReturn(Optional.empty());
                when(coupleCodeCacheService.findUserIdByCode("123-456")).thenReturn(Optional.of("u2"));
                when(authUserCacheService.findById("u2")).thenReturn(Optional.of(recipient));
                when(coupleRequestRepository.findFirstByRequesterUserIdAndStatusOrderByUpdatedAtDesc("u2",
                                CoupleRequestStatus.PENDING))
                                .thenReturn(Optional.empty());
                when(coupleRequestRepository.findFirstByRecipientUserIdAndStatusOrderByUpdatedAtDesc("u2",
                                CoupleRequestStatus.PENDING))
                                .thenReturn(Optional.empty());
                when(coupleRequestRepository.save(any(CoupleRequest.class))).thenAnswer(invocation -> {
                        CoupleRequest request = invocation.getArgument(0);
                        request.setId("req-1");
                        return request;
                });

                CoupleRequestActionResponse response = coupleService.createRequest(
                                "Bearer token-a",
                                new CoupleRequestCreateRequest("123456"));

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
                when(authUserCacheService.findById("u1")).thenReturn(Optional.of(requester));
                when(authUserCacheService.save(any(AuthUser.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));
                when(coupleRequestRepository.save(any(CoupleRequest.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                CoupleRequestActionResponse response = coupleService.decideRequest(
                                "Bearer token-b",
                                "req-accept",
                                new CoupleRequestDecisionRequest(true));

                assertTrue(response.success());
                assertEquals("ACCEPTED", response.status());
                assertEquals("u2", requester.getPartnerUserId());
                assertEquals("u1", recipient.getPartnerUserId());

                verify(authUserCacheService).save(requester);
                verify(authUserCacheService).save(recipient);
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
                when(authUserCacheService.findById("u1")).thenReturn(Optional.of(requester));
                when(coupleRequestRepository.save(any(CoupleRequest.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                CoupleRequestActionResponse response = coupleService.decideRequest(
                                "Bearer token-b",
                                "req-reject",
                                new CoupleRequestDecisionRequest(false));

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

        @Test
        void getPartnerProfileReturnsSummaryWhenPaired() {
                AuthUser user = user("u1", "alice");
                user.setPartnerUserId("u2");
                AuthUser partner = user("u2", "bob");
                partner.setFullName("Bob Nguyen");
                partner.setNickName("Bobby");
                partner.setAvatarUrl("https://cdn.example/avatar-bob.png");

                CoupleInfo info = new CoupleInfo();
                info.setId("couple:u1:u2");
                info.setStartAt("2026-01-01T00:00:00Z");

                when(authIdentityService.requireCurrentUser("Bearer token-d")).thenReturn(user);
                when(authUserCacheService.findById("u2")).thenReturn(Optional.of(partner));
                when(coupleInfoRepository.findById("couple:u1:u2")).thenReturn(Optional.of(info));

                var response = coupleService.getPartnerProfile("Bearer token-d");

                assertTrue(response.success());
                assertTrue(response.paired());
                assertEquals("bob", response.username());
                assertEquals("Bob Nguyen", response.fullName());
                assertEquals("Bobby", response.nickName());
                assertEquals("https://cdn.example/avatar-bob.png", response.avatarUrl());
                assertEquals("2026-01-01T00:00:00Z", response.startAt());
        }

        private AuthUser user(String id, String username) {
                AuthUser user = new AuthUser(username, username + "@example.com", "hash", "2026-01-01T00:00:00Z");
                user.setId(id);
                user.setProfileCompleted(true);
                return user;
        }
}
