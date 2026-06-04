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

import java.util.Optional;

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

        @Mock
        private AuthUserRepository authUserRepository;

        @Mock
        private AvatarFrameCatalog avatarFrameCatalog;

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
                assertEquals("demo@example.com", response.email());
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
                                new ProfileUpsertRequest("  Nguyen Van A  ", "  ", "01/04/2026", "nam", null, null));

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
                                                new ProfileUpsertRequest("   ", "Ani", "2026-12-31", "female", null, null)));

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
                                                                "female", null, null)));

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
                                                                "unknown", null, null)));

                assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        }

        @Test
        void upsertProfileUpdatesEmailWhenProvided() {
                AuthUser user = new AuthUser("demo", "old@example.com", "hash", "2026-01-01T00:00:00Z");
                user.setId("user-1");
                when(authIdentityService.requireCurrentUser("Bearer token")).thenReturn(user);
                when(authUserCacheService.save(user)).thenReturn(user);
                when(authUserRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());

                ProfileResponse response = profileService.upsertProfile(
                                "Bearer token",
                                new ProfileUpsertRequest("Nguyen Van A", "Ani", "2026-04-01", "female",
                                                "  New@Example.com  ", null));

                assertEquals("new@example.com", response.email());
                assertEquals("new@example.com", user.getEmail());
        }

        @Test
        void upsertProfileRejectsDuplicateEmail() {
                AuthUser user = new AuthUser("demo", "old@example.com", "hash", "2026-01-01T00:00:00Z");
                user.setId("user-1");
                AuthUser other = new AuthUser("other", "taken@example.com", "hash", "2026-01-01T00:00:00Z");
                other.setId("user-2");
                when(authIdentityService.requireCurrentUser("Bearer token")).thenReturn(user);
                when(authUserRepository.findByEmail("taken@example.com")).thenReturn(Optional.of(other));

                ResponseStatusException exception = assertThrows(
                                ResponseStatusException.class,
                                () -> profileService.upsertProfile(
                                                "Bearer token",
                                                new ProfileUpsertRequest("Nguyen Van A", "Ani", "2026-04-01", "female",
                                                                "taken@example.com", null)));

                assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        }

        @Test
        void upsertProfileKeepsEmailWhenBlank() {
                AuthUser user = new AuthUser("demo", "keep@example.com", "hash", "2026-01-01T00:00:00Z");
                user.setId("user-1");
                when(authIdentityService.requireCurrentUser("Bearer token")).thenReturn(user);
                when(authUserCacheService.save(user)).thenReturn(user);

                ProfileResponse response = profileService.upsertProfile(
                                "Bearer token",
                                new ProfileUpsertRequest("Nguyen Van A", "Ani", "2026-04-01", "female", "   ", null));

                assertEquals("keep@example.com", response.email());
                assertEquals("keep@example.com", user.getEmail());
        }

        @Test
        void updateAvatarFrameWithValidFramePersistsSelection() {
                AuthUser user = new AuthUser("demo", "demo@example.com", "hash", "2026-01-01T00:00:00Z");
                when(authIdentityService.requireCurrentUser("Bearer token")).thenReturn(user);
                when(avatarFrameCatalog.findById("frame_rose"))
                                .thenReturn(Optional.of(new AvatarFrame("frame_rose", "Rose", "rose", "#E91E63")));
                when(authUserCacheService.save(user)).thenReturn(user);

                ProfileResponse response = profileService.updateAvatarFrame("Bearer token", "frame_rose");

                assertEquals("frame_rose", response.avatarFrameId());
                assertEquals("frame_rose", user.getAvatarFrameId());
        }

        @Test
        void updateAvatarFrameWithUnknownFrameThrowsBadRequest() {
                AuthUser user = new AuthUser("demo", "demo@example.com", "hash", "2026-01-01T00:00:00Z");
                when(authIdentityService.requireCurrentUser("Bearer token")).thenReturn(user);
                when(avatarFrameCatalog.findById("frame_unknown")).thenReturn(Optional.empty());

                ResponseStatusException exception = assertThrows(
                                ResponseStatusException.class,
                                () -> profileService.updateAvatarFrame("Bearer token", "frame_unknown"));

                assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        }
}
