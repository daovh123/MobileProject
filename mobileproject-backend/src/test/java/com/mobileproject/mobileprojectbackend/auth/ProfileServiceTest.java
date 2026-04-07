package com.mobileproject.mobileprojectbackend.auth;

import com.mobileproject.mobileprojectbackend.auth.dto.ProfileResponse;
import com.mobileproject.mobileprojectbackend.auth.dto.ProfileUpsertRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

        @Mock
        private AuthIdentityService authIdentityService;

        @Mock
        private AuthUserCacheService authUserCacheService;

        @InjectMocks
        private ProfileService profileService;

        @Test
        void getProfileReturnsCurrentUserData() {
                AuthUser user = new AuthUser("demo", "demo@example.com", "hash", "2026-01-01T00:00:00Z");
                user.setFullName("Nguyen Van A");
                user.setNickName("Ani");
                user.setBirthDate("2026-04-01");
                user.setGender("FEMALE");
                user.setProfileCompleted(true);
                user.setPartnerUserId("partner-1");

                when(authIdentityService.requireCurrentUser("Bearer token")).thenReturn(user);

                ProfileResponse response = profileService.getProfile("Bearer token");

                assertTrue(response.success());
                assertEquals("demo", response.username());
                assertEquals("Nguyen Van A", response.fullName());
                assertEquals("Ani", response.nickName());
                assertEquals("2026-04-01", response.birthDate());
                assertEquals("FEMALE", response.gender());
                assertTrue(response.profileCompleted());
                assertTrue(response.coupleConnected());
        }

        @Test
        void upsertProfileNormalizesBirthDateAndMarksCompleted() {
                AuthUser user = new AuthUser("demo", "demo@example.com", "hash", "2026-01-01T00:00:00Z");
                when(authIdentityService.requireCurrentUser("Bearer token")).thenReturn(user);
                when(authUserCacheService.save(user)).thenReturn(user);

                ProfileResponse response = profileService.upsertProfile(
                                "Bearer token",
                                new ProfileUpsertRequest("  Nguyen Van A  ", "  ", "01/04/2026", "nam"));

                assertTrue(response.success());
                assertEquals("demo", response.username());
                assertEquals("Nguyen Van A", response.fullName());
                assertNull(response.nickName());
                assertEquals("2026-04-01", response.birthDate());
                assertEquals("MALE", response.gender());
                assertTrue(response.profileCompleted());

                ArgumentCaptor<AuthUser> userCaptor = ArgumentCaptor.forClass(AuthUser.class);
                verify(authUserCacheService).save(userCaptor.capture());

                AuthUser savedUser = userCaptor.getValue();
                assertEquals("Nguyen Van A", savedUser.getFullName());
                assertNull(savedUser.getNickName());
                assertEquals("2026-04-01", savedUser.getBirthDate());
                assertEquals("MALE", savedUser.getGender());
                assertTrue(savedUser.isProfileCompleted());
        }

        @Test
        void upsertProfileWithBlankFullNameThrowsBadRequest() {
                AuthUser user = new AuthUser("demo", "demo@example.com", "hash", "2026-01-01T00:00:00Z");
                when(authIdentityService.requireCurrentUser("Bearer token")).thenReturn(user);

                ResponseStatusException exception = assertThrows(
                                ResponseStatusException.class,
                                () -> profileService.upsertProfile(
                                                "Bearer token",
                                                new ProfileUpsertRequest("   ", "Ani", "2026-12-31", "female")));

                assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        }

        @Test
        void upsertProfileWithInvalidBirthDateThrowsBadRequest() {
                AuthUser user = new AuthUser("demo", "demo@example.com", "hash", "2026-01-01T00:00:00Z");
                when(authIdentityService.requireCurrentUser("Bearer token")).thenReturn(user);

                ResponseStatusException exception = assertThrows(
                                ResponseStatusException.class,
                                () -> profileService.upsertProfile(
                                                "Bearer token",
                                                new ProfileUpsertRequest("Nguyen Van A", "Ani", "2026/31/12",
                                                                "female")));

                assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        }

        @Test
        void upsertProfileWithInvalidGenderThrowsBadRequest() {
                AuthUser user = new AuthUser("demo", "demo@example.com", "hash", "2026-01-01T00:00:00Z");
                when(authIdentityService.requireCurrentUser("Bearer token")).thenReturn(user);

                ResponseStatusException exception = assertThrows(
                                ResponseStatusException.class,
                                () -> profileService.upsertProfile(
                                                "Bearer token",
                                                new ProfileUpsertRequest("Nguyen Van A", "Ani", "2026-12-31",
                                                                "unknown")));

                assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        }
}
