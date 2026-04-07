package com.mobileproject.mobileprojectbackend.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthUserCacheServiceTest {

    @Mock
    private AuthUserRepository authUserRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    private AuthUserCacheService authUserCacheService;

    @BeforeEach
    void setUp() {
        authUserCacheService = new AuthUserCacheService(authUserRepository, redisTemplate);
    }

    @Test
    void findByUsernameShouldUseRedisIndexAndAvoidMongoQuery() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);

        when(valueOperations.get("auth:user:username:demo")).thenReturn("u9");
        when(hashOperations.entries("auth:user:id:u9")).thenReturn(Map.of(
                "id", "u9",
                "username", "demo",
                "email", "demo@example.com",
                "passwordHash", "hash",
                "createdAt", "2026-01-01T00:00:00Z",
                "profileCompleted", "true"));

        Optional<AuthUser> result = authUserCacheService.findByUsername("Demo");

        assertTrue(result.isPresent());
        assertEquals("u9", result.get().getId());
        assertEquals("demo", result.get().getUsername());
        verify(authUserRepository, never()).findByUsername(any(String.class));
    }

    @Test
    void findByUsernameWhenRedisUnavailableShouldFallbackToInMemorySnapshot() {
        AuthUser dbUser = createUser("u1", "demo", "demo@example.com");

        when(redisTemplate.opsForValue()).thenThrow(new RuntimeException("redis down"));
        when(authUserRepository.findByUsername("demo")).thenReturn(Optional.of(dbUser));

        Optional<AuthUser> first = authUserCacheService.findByUsername("demo");
        Optional<AuthUser> second = authUserCacheService.findByUsername("demo");

        assertTrue(first.isPresent());
        assertTrue(second.isPresent());
        assertEquals("u1", second.get().getId());
        verify(authUserRepository, times(1)).findByUsername("demo");
    }

    @Test
    void saveShouldWriteUserHashAndIndexesToRedis() {
        AuthUser user = createUser("u2", "alice", "alice@example.com");

        when(authUserRepository.save(any(AuthUser.class))).thenReturn(user);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        AuthUser saved = authUserCacheService.save(user);

        assertEquals("u2", saved.getId());
        verify(hashOperations).putAll(eq("auth:user:id:u2"), anyMap());
        verify(redisTemplate).expire("auth:user:id:u2", 30 * 60, TimeUnit.SECONDS);
        verify(valueOperations).set("auth:user:username:alice", "u2", 30 * 60, TimeUnit.SECONDS);
        verify(valueOperations).set("auth:user:email:alice@example.com", "u2", 30 * 60, TimeUnit.SECONDS);
    }

    private AuthUser createUser(String id, String username, String email) {
        AuthUser user = new AuthUser(username, email, "hash", "2026-01-01T00:00:00Z");
        user.setId(id);
        user.setProfileCompleted(true);
        return user;
    }
}
