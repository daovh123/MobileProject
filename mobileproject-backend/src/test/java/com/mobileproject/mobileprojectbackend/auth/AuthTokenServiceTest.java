package com.mobileproject.mobileprojectbackend.auth;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthTokenServiceTest {

    private static final String TEST_SECRET = "test-secret-for-token-unit-tests";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String TOKEN_VERSION = "v1";

    private final AuthTokenService authTokenService = new AuthTokenService(TEST_SECRET, 604800);

    @Test
    void malformedTokenIsRejected() {
        Optional<String> resolvedUsername = authTokenService.resolveUsername("Bearer malformed-token");

        assertTrue(resolvedUsername.isEmpty());
    }

    @Test
    void tamperedSignatureIsRejected() {
        String issuedToken = authTokenService.issueToken("alice");
        String[] tokenParts = issuedToken.split("\\.");
        assertEquals(3, tokenParts.length);

        String tamperedToken = tokenParts[0] + "." + tokenParts[1] + "." + tokenParts[2] + "tampered";
        Optional<String> resolvedUsername = authTokenService.resolveUsername("Bearer " + tamperedToken);

        assertTrue(resolvedUsername.isEmpty());
    }

    @Test
    void revokedTokenIsRejected() {
        String issuedToken = authTokenService.issueToken("bob");

        assertTrue(authTokenService.resolveUsername("Bearer " + issuedToken).isPresent());
        assertTrue(authTokenService.revokeToken("Bearer " + issuedToken));
        assertTrue(authTokenService.resolveUsername("Bearer " + issuedToken).isEmpty());
    }

    @Test
    void expiredTokenIsRejected() {
        long expiredAtEpochSeconds = Instant.now().minusSeconds(60).getEpochSecond();
        String expiredToken = createSignedToken("charlie", expiredAtEpochSeconds, UUID.randomUUID().toString());

        Optional<String> resolvedUsername = authTokenService.resolveUsername("Bearer " + expiredToken);

        assertTrue(resolvedUsername.isEmpty());
    }

    @Test
    void constructorRejectsEmptyOrBlankSecret() {
        assertThrows(IllegalStateException.class, () -> new AuthTokenService("", 604800));
        assertThrows(IllegalStateException.class, () -> new AuthTokenService("   ", 604800));
    }

    private String createSignedToken(String username, long expiresAtEpochSeconds, String tokenId) {
        String normalizedUsername = username.trim().toLowerCase(Locale.ROOT);
        String payload = normalizedUsername + "|" + expiresAtEpochSeconds + "|" + tokenId;
        String payloadSegment = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        String signatureSegment = sign(payloadSegment);

        return TOKEN_VERSION + "." + payloadSegment + "." + signatureSegment;
    }

    private String sign(String payloadSegment) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(TEST_SECRET.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            byte[] signature = mac.doFinal(payloadSegment.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot sign test token", exception);
        }
    }
}