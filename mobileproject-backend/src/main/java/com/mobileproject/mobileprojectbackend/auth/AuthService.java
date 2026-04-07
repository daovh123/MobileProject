package com.mobileproject.mobileprojectbackend.auth;

import com.mobileproject.mobileprojectbackend.auth.dto.AuthResponse;
import com.mobileproject.mobileprojectbackend.auth.dto.LoginRequest;
import com.mobileproject.mobileprojectbackend.auth.dto.LogoutResponse;
import com.mobileproject.mobileprojectbackend.auth.dto.RegisterRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

@Service
public class AuthService {

    private final AuthUserRepository authUserRepository;
    private final AuthUserCacheService authUserCacheService;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenService authTokenService;

    public AuthService(AuthUserRepository authUserRepository,
            AuthUserCacheService authUserCacheService,
            PasswordEncoder passwordEncoder,
            AuthTokenService authTokenService) {
        this.authUserRepository = authUserRepository;
        this.authUserCacheService = authUserCacheService;
        this.passwordEncoder = passwordEncoder;
        this.authTokenService = authTokenService;
    }

    public AuthResponse register(RegisterRequest request) {
        String usernameKey = normalize(request.username());
        String emailKey = normalize(request.email());

        if (authUserRepository.existsByUsername(usernameKey)) {
            return AuthResponse.failure("Username already exists");
        }
        if (authUserRepository.existsByEmail(emailKey)) {
            return AuthResponse.failure("Email already exists");
        }

        AuthUser user = new AuthUser(
                usernameKey,
                emailKey,
                passwordEncoder.encode(request.password()),
                Instant.now().toString());

        AuthUser savedUser = authUserCacheService.save(user);

        return AuthResponse.success(
                "Register successful",
                issueDevToken(usernameKey),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.isProfileCompleted(),
                isCoupleConnected(savedUser));
    }

    public AuthResponse login(LoginRequest request) {
        String loginKey = normalize(request.usernameOrEmail());

        Optional<AuthUser> userOptional = authUserCacheService.findByUsername(loginKey);
        if (userOptional.isEmpty()) {
            userOptional = authUserCacheService.findByEmail(loginKey);
        }

        if (userOptional.isEmpty()
                || !passwordEncoder.matches(request.password(), userOptional.get().getPasswordHash())) {
            return AuthResponse.failure("Invalid username/email or password");
        }

        AuthUser user = userOptional.get();

        return AuthResponse.success(
                "Login successful",
                issueDevToken(user.getUsername()),
                user.getUsername(),
                user.getEmail(),
                user.isProfileCompleted(),
                isCoupleConnected(user));
    }

    public LogoutResponse logout(String authorizationHeader) {
        boolean revoked = authTokenService.revokeToken(authorizationHeader);
        if (!revoked) {
            return LogoutResponse.failure("Missing or invalid access token");
        }
        return LogoutResponse.success("Logout successful");
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String issueDevToken(String username) {
        return authTokenService.issueToken(username);
    }

    private boolean isCoupleConnected(AuthUser user) {
        return user.getPartnerUserId() != null && !user.getPartnerUserId().isBlank();
    }
}
