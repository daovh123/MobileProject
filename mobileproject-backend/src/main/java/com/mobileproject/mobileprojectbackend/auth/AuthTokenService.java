package com.mobileproject.mobileprojectbackend.auth;

import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthTokenService {

    private final Map<String, String> issuedTokens = new ConcurrentHashMap<>();

    public String issueToken(String username) {
        String token = "dev-" + username + "-" + UUID.randomUUID();
        issuedTokens.put(token, username);
        return token;
    }

    public Optional<String> resolveUsername(String authorizationHeader) {
        return extractToken(authorizationHeader)
                .map(issuedTokens::get);
    }

    public boolean revokeToken(String authorizationHeader) {
        return extractToken(authorizationHeader)
                .map(token -> issuedTokens.remove(token) != null)
                .orElse(false);
    }

    private Optional<String> extractToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return Optional.empty();
        }

        String raw = authorizationHeader.trim();
        String lower = raw.toLowerCase(Locale.ROOT);
        String token = lower.startsWith("bearer ") ? raw.substring(7).trim() : raw;
        if (token.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(token);
    }
}
