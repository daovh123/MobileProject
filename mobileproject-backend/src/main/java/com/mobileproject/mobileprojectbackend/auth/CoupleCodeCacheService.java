package com.mobileproject.mobileprojectbackend.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class CoupleCodeCacheService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoupleCodeCacheService.class);
    private static final int MAX_CODE_RETRY = 20;
    private static final long CODE_TTL_SECONDS = 15 * 60;

    private final StringRedisTemplate redisTemplate;
    private final SecureRandom secureRandom;

    private final Object inMemoryLock = new Object();
    private final Map<String, LocalUserCodeEntry> inMemoryUserCode = new HashMap<>();
    private final Map<String, LocalCodeLookupEntry> inMemoryCodeLookup = new HashMap<>();

    public CoupleCodeCacheService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.secureRandom = new SecureRandom();
    }

    public CoupleCodeLease getOrCreateCode(String userId) {
        try {
            return getOrCreateCodeFromRedis(userId);
        } catch (RuntimeException redisError) {
            LOGGER.warn(
                    "Redis unavailable while resolving couple code for user {}. Falling back to in-memory cache. Cause: {}",
                    userId,
                    redisError.getMessage()
            );
            return getOrCreateCodeInMemory(userId);
        }
    }

    public Optional<String> findUserIdByCode(String code) {
        try {
            String userId = redisTemplate.opsForValue().get(lookupKey(code));
            return Optional.ofNullable(userId).filter(value -> !value.isBlank());
        } catch (RuntimeException redisError) {
            LOGGER.warn(
                    "Redis unavailable while looking up couple code {}. Falling back to in-memory cache. Cause: {}",
                    code,
                    redisError.getMessage()
            );
            return findUserIdByCodeInMemory(code);
        }
    }

    public void invalidateCodeByUserId(String userId) {
        try {
            String userKey = userKey(userId);
            String code = redisTemplate.opsForValue().get(userKey);
            redisTemplate.delete(userKey);
            if (code != null && !code.isBlank()) {
                redisTemplate.delete(lookupKey(code));
            }
            return;
        } catch (RuntimeException redisError) {
            LOGGER.warn(
                    "Redis unavailable while invalidating couple code for user {}. Falling back to in-memory cache. Cause: {}",
                    userId,
                    redisError.getMessage()
            );
        }

        invalidateCodeByUserIdInMemory(userId);
    }

    private CoupleCodeLease getOrCreateCodeFromRedis(String userId) {
        String userKey = userKey(userId);
        String existingCode = redisTemplate.opsForValue().get(userKey);
        Long existingTtlSeconds = redisTemplate.getExpire(userKey, TimeUnit.SECONDS);

        if (existingCode != null && !existingCode.isBlank() && existingTtlSeconds != null && existingTtlSeconds > 0) {
            String codeKey = lookupKey(existingCode);
            String mappedUserId = redisTemplate.opsForValue().get(codeKey);

            if (userId.equals(mappedUserId)) {
                return new CoupleCodeLease(existingCode, Instant.now().plusSeconds(existingTtlSeconds));
            }

            Boolean reserved = redisTemplate.opsForValue().setIfAbsent(
                    codeKey,
                    userId,
                    existingTtlSeconds,
                    TimeUnit.SECONDS
            );
            if (Boolean.TRUE.equals(reserved)) {
                return new CoupleCodeLease(existingCode, Instant.now().plusSeconds(existingTtlSeconds));
            }

            mappedUserId = redisTemplate.opsForValue().get(codeKey);
            if (userId.equals(mappedUserId)) {
                return new CoupleCodeLease(existingCode, Instant.now().plusSeconds(existingTtlSeconds));
            }

            redisTemplate.delete(userKey);
        }

        for (int attempt = 0; attempt < MAX_CODE_RETRY; attempt++) {
            String candidate = randomCode();
            String candidateLookupKey = lookupKey(candidate);

            Boolean reserved = redisTemplate.opsForValue().setIfAbsent(
                    candidateLookupKey,
                    userId,
                    CODE_TTL_SECONDS,
                    TimeUnit.SECONDS
            );

            if (Boolean.TRUE.equals(reserved)) {
                redisTemplate.opsForValue().set(userKey, candidate, CODE_TTL_SECONDS, TimeUnit.SECONDS);
                return new CoupleCodeLease(candidate, Instant.now().plusSeconds(CODE_TTL_SECONDS));
            }
        }

        throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unable to generate couple code, please try again"
        );
    }

    private CoupleCodeLease getOrCreateCodeInMemory(String userId) {
        synchronized (inMemoryLock) {
            clearExpiredInMemoryEntries();

            LocalUserCodeEntry existing = inMemoryUserCode.get(userId);
            if (existing != null && existing.expiresAt().isAfter(Instant.now())) {
                LocalCodeLookupEntry mapped = inMemoryCodeLookup.get(existing.code());
                if (mapped != null
                        && mapped.expiresAt().isAfter(Instant.now())
                        && userId.equals(mapped.userId())) {
                    return new CoupleCodeLease(existing.code(), existing.expiresAt());
                }
                inMemoryUserCode.remove(userId);
            }

            for (int attempt = 0; attempt < MAX_CODE_RETRY; attempt++) {
                String candidate = randomCode();
                LocalCodeLookupEntry current = inMemoryCodeLookup.get(candidate);
                if (current != null && current.expiresAt().isAfter(Instant.now())) {
                    continue;
                }

                Instant expiresAt = Instant.now().plusSeconds(CODE_TTL_SECONDS);
                inMemoryCodeLookup.put(candidate, new LocalCodeLookupEntry(userId, expiresAt));
                inMemoryUserCode.put(userId, new LocalUserCodeEntry(candidate, expiresAt));
                return new CoupleCodeLease(candidate, expiresAt);
            }

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to generate couple code, please try again"
            );
        }
    }

    private Optional<String> findUserIdByCodeInMemory(String code) {
        synchronized (inMemoryLock) {
            clearExpiredInMemoryEntries();
            LocalCodeLookupEntry lookupEntry = inMemoryCodeLookup.get(code);
            if (lookupEntry == null || lookupEntry.expiresAt().isBefore(Instant.now())) {
                return Optional.empty();
            }
            return Optional.of(lookupEntry.userId());
        }
    }

    private void invalidateCodeByUserIdInMemory(String userId) {
        synchronized (inMemoryLock) {
            LocalUserCodeEntry codeEntry = inMemoryUserCode.remove(userId);
            if (codeEntry != null) {
                inMemoryCodeLookup.remove(codeEntry.code());
            }
        }
    }

    private void clearExpiredInMemoryEntries() {
        Instant now = Instant.now();

        inMemoryCodeLookup.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
        inMemoryUserCode.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
    }

    private String randomCode() {
        return String.format("%03d-%03d", secureRandom.nextInt(1000), secureRandom.nextInt(1000));
    }

    private String userKey(String userId) {
        return "couple:code:user:" + userId;
    }

    private String lookupKey(String code) {
        return "couple:code:lookup:" + code;
    }

    public record CoupleCodeLease(String code, Instant expiresAt) {
    }

    private record LocalUserCodeEntry(String code, Instant expiresAt) {
    }

    private record LocalCodeLookupEntry(String userId, Instant expiresAt) {
    }
}
