package com.mobileproject.mobileprojectbackend.auth;

import com.mobileproject.mobileprojectbackend.auth.dto.AuthResponse;
import com.mobileproject.mobileprojectbackend.auth.dto.LoginRequest;
import com.mobileproject.mobileprojectbackend.auth.dto.LogoutResponse;
import com.mobileproject.mobileprojectbackend.auth.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthUserRepository authUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthTokenService authTokenService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerSuccessSavesUserToMongoRepository() {
        RegisterRequest request = new RegisterRequest("Demo", "demo@example.com", "123456");

        when(authUserRepository.existsByUsername("demo")).thenReturn(false);
        when(authUserRepository.existsByEmail("demo@example.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("hashed-password");
        when(authTokenService.issueToken("demo")).thenReturn("token-demo");

        AuthResponse response = authService.register(request);

        assertTrue(response.success());
        assertEquals("demo", response.username());
        assertEquals("demo@example.com", response.email());
        assertFalse(response.profileCompleted());
        assertFalse(response.coupleConnected());

        ArgumentCaptor<AuthUser> userCaptor = ArgumentCaptor.forClass(AuthUser.class);
        verify(authUserRepository).save(userCaptor.capture());

        AuthUser savedUser = userCaptor.getValue();
        assertEquals("demo", savedUser.getUsername());
        assertEquals("demo@example.com", savedUser.getEmail());
        assertEquals("hashed-password", savedUser.getPasswordHash());
    }

    @Test
    void registerDuplicateUsernameReturnsFailure() {
        RegisterRequest request = new RegisterRequest("Demo", "demo@example.com", "123456");

        when(authUserRepository.existsByUsername("demo")).thenReturn(true);

        AuthResponse response = authService.register(request);

        assertFalse(response.success());
        assertEquals("Username already exists", response.message());
        verify(authUserRepository, never()).save(any(AuthUser.class));
    }

    @Test
    void loginByEmailSuccess() {
        LoginRequest request = new LoginRequest("demo@example.com", "123456");

        AuthUser user = new AuthUser("demo", "demo@example.com", "hashed-password", "2026-01-01T00:00:00Z");

        when(authUserRepository.findByUsername("demo@example.com")).thenReturn(Optional.empty());
        when(authUserRepository.findByEmail("demo@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(eq("123456"), eq("hashed-password"))).thenReturn(true);
        when(authTokenService.issueToken("demo")).thenReturn("token-demo");

        AuthResponse response = authService.login(request);

        assertTrue(response.success());
        assertEquals("demo", response.username());
        assertEquals("demo@example.com", response.email());
        assertFalse(response.profileCompleted());
        assertFalse(response.coupleConnected());
    }

    @Test
    void logoutWithValidTokenReturnsSuccess() {
        when(authTokenService.revokeToken("Bearer token-demo")).thenReturn(true);

        LogoutResponse response = authService.logout("Bearer token-demo");

        assertTrue(response.success());
        assertEquals("Logout successful", response.message());
    }

    @Test
    void logoutWithInvalidTokenReturnsFailure() {
        when(authTokenService.revokeToken("Bearer invalid")).thenReturn(false);

        LogoutResponse response = authService.logout("Bearer invalid");

        assertFalse(response.success());
        assertEquals("Missing or invalid access token", response.message());
    }
}
