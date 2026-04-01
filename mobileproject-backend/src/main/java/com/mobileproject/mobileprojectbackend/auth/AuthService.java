package com.mobileproject.mobileprojectbackend.auth;

import com.mobileproject.mobileprojectbackend.auth.dto.AuthResponse;
import com.mobileproject.mobileprojectbackend.auth.dto.LoginRequest;
import com.mobileproject.mobileprojectbackend.auth.dto.RegisterRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthUserRepository authUserRepository, PasswordEncoder passwordEncoder) {
        this.authUserRepository = authUserRepository;
        this.passwordEncoder = passwordEncoder;
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
                Instant.now().toString()
        );

        authUserRepository.save(user);

        return AuthResponse.success(
                "Register successful",
                issueDevToken(usernameKey),
                user.getUsername(),
                user.getEmail()
        );
    }

    public AuthResponse login(LoginRequest request) {
        String loginKey = normalize(request.usernameOrEmail());

        Optional<AuthUser> userOptional = authUserRepository.findByUsername(loginKey);
        if (userOptional.isEmpty()) {
            userOptional = authUserRepository.findByEmail(loginKey);
        }

        if (userOptional.isEmpty() || !passwordEncoder.matches(request.password(), userOptional.get().getPasswordHash())) {
            return AuthResponse.failure("Invalid username/email or password");
        }

        AuthUser user = userOptional.get();

        return AuthResponse.success(
                "Login successful",
                issueDevToken(user.getUsername()),
                user.getUsername(),
                user.getEmail()
        );
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String issueDevToken(String username) {
        return "dev-" + username + "-" + UUID.randomUUID();
    }
}
