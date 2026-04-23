package com.mobileproject.mobileprojectbackend.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Service
public class AuthTokenService {

    private static final String TOKEN_VERSION = "v1";
    private static final long DEFAULT_TOKEN_TTL_SECONDS = 60L * 60L * 24L * 7L;
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final byte[] signingSecret;
    private final long tokenTtlSeconds;
    private final Map<String, Long> revokedTokenIds = new ConcurrentHashMap<>();

    public AuthTokenService(
            @Value("${app.auth.token-secret:}") String configuredSecret,
            @Value("${app.auth.token-ttl-seconds:604800}") long configuredTokenTtlSeconds) {
        this.signingSecret = resolveSigningSecret(configuredSecret);
        this.tokenTtlSeconds = configuredTokenTtlSeconds > 0
                ? configuredTokenTtlSeconds
                : DEFAULT_TOKEN_TTL_SECONDS;
    }

    public String issueToken(String username) {
        String normalizedUsername = normalizeUsername(username);
        long expiresAtEpochSeconds = (System.currentTimeMillis() / 1000L) + tokenTtlSeconds;
        String tokenId = UUID.randomUUID().toString();

        String payloadSegment = encodePayload(normalizedUsername, expiresAtEpochSeconds, tokenId);
        String signatureSegment = sign(payloadSegment);

        return TOKEN_VERSION + "." + payloadSegment + "." + signatureSegment;
    }

    public Optional<String> resolveUsername(String authorizationHeader) {
        return extractToken(authorizationHeader)
                .flatMap(this::parseVerifiedToken)
                .map(TokenClaims::username);
    }

    public boolean revokeToken(String authorizationHeader) {
        long nowEpochSeconds = System.currentTimeMillis() / 1000L;
        cleanupExpiredRevocations(nowEpochSeconds);

        return extractToken(authorizationHeader)
                .flatMap(this::parseVerifiedToken)
                .map(claims -> {
                    revokedTokenIds.put(claims.tokenId(), claims.expiresAtEpochSeconds());
                    return true;
                })
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

    private Optional<TokenClaims> parseVerifiedToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return Optional.empty();
        }
        if (!TOKEN_VERSION.equals(parts[0])) {
            return Optional.empty();
        }

        String payloadSegment = parts[1];
        String signatureSegment = parts[2];
        String expectedSignature = sign(payloadSegment);
        if (!MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                signatureSegment.getBytes(StandardCharsets.UTF_8))) {
            return Optional.empty();
        }

        byte[] payloadBytes;
        try {
            payloadBytes = Base64.getUrlDecoder().decode(payloadSegment);
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }

        String decodedPayload = new String(payloadBytes, StandardCharsets.UTF_8);
        String[] payloadParts = decodedPayload.split("\\|", 3);
        if (payloadParts.length != 3) {
            return Optional.empty();
        }

        String username = normalizeUsername(payloadParts[0]);
        long expiresAtEpochSeconds;
        try {
            expiresAtEpochSeconds = Long.parseLong(payloadParts[1]);
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }

        String tokenId = payloadParts[2].trim();
        if (username.isBlank() || tokenId.isBlank()) {
            return Optional.empty();
        }

        long nowEpochSeconds = System.currentTimeMillis() / 1000L;
        cleanupExpiredRevocations(nowEpochSeconds);

        if (expiresAtEpochSeconds <= nowEpochSeconds) {
            return Optional.empty();
        }

        Long revokedUntil = revokedTokenIds.get(tokenId);
        if (revokedUntil != null && revokedUntil >= nowEpochSeconds) {
            return Optional.empty();
        }

        return Optional.of(new TokenClaims(username, expiresAtEpochSeconds, tokenId));
    }

    private String encodePayload(String username, long expiresAtEpochSeconds, String tokenId) {
        String rawPayload = username + "|" + expiresAtEpochSeconds + "|" + tokenId;
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(rawPayload.getBytes(StandardCharsets.UTF_8));
    }

    private String sign(String payloadSegment) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(signingSecret, HMAC_ALGORITHM));
            byte[] signature = mac.doFinal(payloadSegment.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot sign access token", exception);
        }
    }

    private byte[] resolveSigningSecret(String configuredSecret) {
        String normalized = configuredSecret == null ? "" : configuredSecret.trim();
        if (normalized.isBlank()) {
            throw new IllegalStateException(
                    "app.auth.token-secret must not be empty. Configure APP_AUTH_TOKEN_SECRET.");
        }
        return normalized.getBytes(StandardCharsets.UTF_8);
    }

    private void cleanupExpiredRevocations(long nowEpochSeconds) {
        revokedTokenIds.entrySet().removeIf(entry -> entry.getValue() <= nowEpochSeconds);
    }

    private String normalizeUsername(String username) {
        if (username == null) {
            return "";
        }
        return username.trim().toLowerCase(Locale.ROOT);
    }

    private record TokenClaims(String username, long expiresAtEpochSeconds, String tokenId) {
    }
}
