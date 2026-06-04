package com.mobileproject.mobileprojectbackend.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class AuthUserCacheService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthUserCacheService.class);
    private static final long USER_TTL_SECONDS = 30 * 60;

    private final AuthUserRepository authUserRepository;
    private final StringRedisTemplate redisTemplate;

    private final Object inMemoryLock = new Object();
    private final Map<String, LocalUserEntry> inMemoryUserById = new HashMap<>();
    private final Map<String, LocalIndexEntry> inMemoryUserIdByUsername = new HashMap<>();
    private final Map<String, LocalIndexEntry> inMemoryUserIdByEmail = new HashMap<>();

    public AuthUserCacheService(AuthUserRepository authUserRepository, StringRedisTemplate redisTemplate) {
        this.authUserRepository = authUserRepository;
        this.redisTemplate = redisTemplate;
    }

    public Optional<AuthUser> findById(String userId) {
        if (isBlank(userId)) {
            return Optional.empty();
        }

        try {
            Optional<AuthUser> cached = findByIdFromRedis(userId);
            if (cached.isPresent()) {
                return cached;
            }

            Optional<AuthUser> dbUser = authUserRepository.findById(userId);
            dbUser.ifPresent(this::cacheUser);
            return dbUser;
        } catch (RuntimeException redisError) {
            LOGGER.warn(
                    "Redis unavailable while loading user by id {}. Falling back to in-memory cache. Cause: {}",
                    userId,
                    redisError.getMessage());

            Optional<AuthUser> cached = findByIdInMemory(userId);
            if (cached.isPresent()) {
                return cached;
            }

            Optional<AuthUser> dbUser = authUserRepository.findById(userId);
            dbUser.ifPresent(this::cacheUserInMemory);
            return dbUser;
        }
    }

    public Optional<AuthUser> findByUsername(String username) {
        String normalizedUsername = normalizeKey(username);
        if (isBlank(normalizedUsername)) {
            return Optional.empty();
        }

        try {
            Optional<AuthUser> cached = findByUsernameFromRedis(normalizedUsername);
            if (cached.isPresent()) {
                return cached;
            }

            Optional<AuthUser> dbUser = authUserRepository.findByUsername(normalizedUsername);
            dbUser.ifPresent(this::cacheUser);
            return dbUser;
        } catch (RuntimeException redisError) {
            LOGGER.warn(
                    "Redis unavailable while loading user by username {}. Falling back to in-memory cache. Cause: {}",
                    normalizedUsername,
                    redisError.getMessage());

            Optional<AuthUser> cached = findByUsernameInMemory(normalizedUsername);
            if (cached.isPresent()) {
                return cached;
            }

            Optional<AuthUser> dbUser = authUserRepository.findByUsername(normalizedUsername);
            dbUser.ifPresent(this::cacheUserInMemory);
            return dbUser;
        }
    }

    public Optional<AuthUser> findByEmail(String email) {
        String normalizedEmail = normalizeKey(email);
        if (isBlank(normalizedEmail)) {
            return Optional.empty();
        }

        try {
            Optional<AuthUser> cached = findByEmailFromRedis(normalizedEmail);
            if (cached.isPresent()) {
                return cached;
            }

            Optional<AuthUser> dbUser = authUserRepository.findByEmail(normalizedEmail);
            dbUser.ifPresent(this::cacheUser);
            return dbUser;
        } catch (RuntimeException redisError) {
            LOGGER.warn(
                    "Redis unavailable while loading user by email {}. Falling back to in-memory cache. Cause: {}",
                    normalizedEmail,
                    redisError.getMessage());

            Optional<AuthUser> cached = findByEmailInMemory(normalizedEmail);
            if (cached.isPresent()) {
                return cached;
            }

            Optional<AuthUser> dbUser = authUserRepository.findByEmail(normalizedEmail);
            dbUser.ifPresent(this::cacheUserInMemory);
            return dbUser;
        }
    }

    public AuthUser save(AuthUser user) {
        AuthUser saved = authUserRepository.save(user);
        cacheUser(saved);
        return saved;
    }

    public void cacheUser(AuthUser user) {
        if (user == null || isBlank(user.getId())) {
            return;
        }

        try {
            cacheUserToRedis(user);
        } catch (RuntimeException redisError) {
            LOGGER.warn(
                    "Redis unavailable while caching user id {}. Falling back to in-memory cache. Cause: {}",
                    user.getId(),
                    redisError.getMessage());
        }

        cacheUserInMemory(user);
    }

    private Optional<AuthUser> findByIdFromRedis(String userId) {
        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
        Map<Object, Object> fields = hashOperations.entries(userByIdKey(userId));
        if (fields == null || fields.isEmpty()) {
            return Optional.empty();
        }

        AuthUser user = mapToUser(fields);
        if (user == null || isBlank(user.getId())) {
            return Optional.empty();
        }

        refreshRedisTtl(user);
        return Optional.of(user);
    }

    private Optional<AuthUser> findByUsernameFromRedis(String normalizedUsername) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String userId = valueOperations.get(usernameToUserIdKey(normalizedUsername));
        if (isBlank(userId)) {
            return Optional.empty();
        }

        Optional<AuthUser> user = findByIdFromRedis(userId);
        if (user.isEmpty()) {
            redisTemplate.delete(usernameToUserIdKey(normalizedUsername));
            return Optional.empty();
        }

        redisTemplate.expire(usernameToUserIdKey(normalizedUsername), USER_TTL_SECONDS, TimeUnit.SECONDS);
        return user;
    }

    private Optional<AuthUser> findByEmailFromRedis(String normalizedEmail) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String userId = valueOperations.get(emailToUserIdKey(normalizedEmail));
        if (isBlank(userId)) {
            return Optional.empty();
        }

        Optional<AuthUser> user = findByIdFromRedis(userId);
        if (user.isEmpty()) {
            redisTemplate.delete(emailToUserIdKey(normalizedEmail));
            return Optional.empty();
        }

        redisTemplate.expire(emailToUserIdKey(normalizedEmail), USER_TTL_SECONDS, TimeUnit.SECONDS);
        return user;
    }

    private void cacheUserToRedis(AuthUser user) {
        String userId = user.getId();
        String normalizedUsername = normalizeKey(user.getUsername());
        String normalizedEmail = normalizeKey(user.getEmail());

        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
        hashOperations.putAll(userByIdKey(userId), toHash(user));
        redisTemplate.expire(userByIdKey(userId), USER_TTL_SECONDS, TimeUnit.SECONDS);

        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        if (!isBlank(normalizedUsername)) {
            valueOperations.set(usernameToUserIdKey(normalizedUsername), userId, USER_TTL_SECONDS, TimeUnit.SECONDS);
        }
        if (!isBlank(normalizedEmail)) {
            valueOperations.set(emailToUserIdKey(normalizedEmail), userId, USER_TTL_SECONDS, TimeUnit.SECONDS);
        }
    }

    private void refreshRedisTtl(AuthUser user) {
        redisTemplate.expire(userByIdKey(user.getId()), USER_TTL_SECONDS, TimeUnit.SECONDS);

        String normalizedUsername = normalizeKey(user.getUsername());
        if (!isBlank(normalizedUsername)) {
            redisTemplate.expire(usernameToUserIdKey(normalizedUsername), USER_TTL_SECONDS, TimeUnit.SECONDS);
        }

        String normalizedEmail = normalizeKey(user.getEmail());
        if (!isBlank(normalizedEmail)) {
            redisTemplate.expire(emailToUserIdKey(normalizedEmail), USER_TTL_SECONDS, TimeUnit.SECONDS);
        }
    }

    private Optional<AuthUser> findByIdInMemory(String userId) {
        synchronized (inMemoryLock) {
            clearExpiredInMemoryEntries();
            LocalUserEntry entry = inMemoryUserById.get(userId);
            if (entry == null) {
                return Optional.empty();
            }
            return Optional.of(copyUser(entry.userSnapshot()));
        }
    }

    private Optional<AuthUser> findByUsernameInMemory(String normalizedUsername) {
        synchronized (inMemoryLock) {
            clearExpiredInMemoryEntries();

            LocalIndexEntry index = inMemoryUserIdByUsername.get(normalizedUsername);
            if (index == null || isBlank(index.userId())) {
                return Optional.empty();
            }

            LocalUserEntry userEntry = inMemoryUserById.get(index.userId());
            if (userEntry == null) {
                inMemoryUserIdByUsername.remove(normalizedUsername);
                return Optional.empty();
            }

            return Optional.of(copyUser(userEntry.userSnapshot()));
        }
    }

    private Optional<AuthUser> findByEmailInMemory(String normalizedEmail) {
        synchronized (inMemoryLock) {
            clearExpiredInMemoryEntries();

            LocalIndexEntry index = inMemoryUserIdByEmail.get(normalizedEmail);
            if (index == null || isBlank(index.userId())) {
                return Optional.empty();
            }

            LocalUserEntry userEntry = inMemoryUserById.get(index.userId());
            if (userEntry == null) {
                inMemoryUserIdByEmail.remove(normalizedEmail);
                return Optional.empty();
            }

            return Optional.of(copyUser(userEntry.userSnapshot()));
        }
    }

    private void cacheUserInMemory(AuthUser user) {
        synchronized (inMemoryLock) {
            clearExpiredInMemoryEntries();

            Instant expiresAt = Instant.now().plusSeconds(USER_TTL_SECONDS);
            AuthUser snapshot = copyUser(user);
            inMemoryUserById.put(snapshot.getId(), new LocalUserEntry(snapshot, expiresAt));

            String normalizedUsername = normalizeKey(snapshot.getUsername());
            if (!isBlank(normalizedUsername)) {
                inMemoryUserIdByUsername.put(normalizedUsername, new LocalIndexEntry(snapshot.getId(), expiresAt));
            }

            String normalizedEmail = normalizeKey(snapshot.getEmail());
            if (!isBlank(normalizedEmail)) {
                inMemoryUserIdByEmail.put(normalizedEmail, new LocalIndexEntry(snapshot.getId(), expiresAt));
            }
        }
    }

    private void clearExpiredInMemoryEntries() {
        Instant now = Instant.now();

        inMemoryUserById.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
        inMemoryUserIdByUsername.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
        inMemoryUserIdByEmail.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
    }

    private Map<String, String> toHash(AuthUser user) {
        Map<String, String> hash = new HashMap<>();
        put(hash, "id", user.getId());
        put(hash, "username", normalizeKey(user.getUsername()));
        put(hash, "email", normalizeKey(user.getEmail()));
        put(hash, "passwordHash", user.getPasswordHash());
        put(hash, "createdAt", user.getCreatedAt());
        put(hash, "fullName", user.getFullName());
        put(hash, "nickName", user.getNickName());
        put(hash, "birthDate", user.getBirthDate());
        put(hash, "gender", user.getGender());
        put(hash, "phoneNumber", user.getPhoneNumber());
        put(hash, "partnerUserId", user.getPartnerUserId());
        put(hash, "avatarUrl", user.getAvatarUrl());
        put(hash, "avatarFrameId", user.getAvatarFrameId());
        hash.put("profileCompleted", Boolean.toString(user.isProfileCompleted()));
        return hash;
    }

    private AuthUser mapToUser(Map<Object, Object> values) {
        String id = nullableValue(values.get("id"));
        if (isBlank(id)) {
            return null;
        }

        AuthUser user = new AuthUser();
        user.setId(id);
        user.setUsername(normalizeKey(nullableValue(values.get("username"))));
        user.setEmail(normalizeKey(nullableValue(values.get("email"))));
        user.setPasswordHash(nullableValue(values.get("passwordHash")));
        user.setCreatedAt(nullableValue(values.get("createdAt")));
        user.setFullName(nullableValue(values.get("fullName")));
        user.setNickName(nullableValue(values.get("nickName")));
        user.setBirthDate(nullableValue(values.get("birthDate")));
        user.setGender(nullableValue(values.get("gender")));
        user.setPhoneNumber(nullableValue(values.get("phoneNumber")));
        user.setPartnerUserId(nullableValue(values.get("partnerUserId")));
        user.setAvatarUrl(nullableValue(values.get("avatarUrl")));
        user.setAvatarFrameId(nullableValue(values.get("avatarFrameId")));
        user.setProfileCompleted(Boolean.parseBoolean(nullableValue(values.get("profileCompleted"))));
        return user;
    }

    private AuthUser copyUser(AuthUser source) {
        AuthUser copy = new AuthUser();
        copy.setId(source.getId());
        copy.setUsername(source.getUsername());
        copy.setEmail(source.getEmail());
        copy.setPasswordHash(source.getPasswordHash());
        copy.setCreatedAt(source.getCreatedAt());
        copy.setFullName(source.getFullName());
        copy.setNickName(source.getNickName());
        copy.setBirthDate(source.getBirthDate());
        copy.setGender(source.getGender());
        copy.setPhoneNumber(source.getPhoneNumber());
        copy.setProfileCompleted(source.isProfileCompleted());
        copy.setPartnerUserId(source.getPartnerUserId());
        copy.setAvatarUrl(source.getAvatarUrl());
        copy.setAvatarFrameId(source.getAvatarFrameId());
        return copy;
    }

    private String nullableValue(Object value) {
        if (value == null) {
            return null;
        }
        String text = value.toString();
        return text.isBlank() ? null : text;
    }

    private void put(Map<String, String> target, String key, String value) {
        if (value != null) {
            target.put(key, value);
        }
    }

    private String normalizeKey(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String userByIdKey(String userId) {
        return "auth:user:id:" + userId;
    }

    private String usernameToUserIdKey(String normalizedUsername) {
        return "auth:user:username:" + normalizedUsername;
    }

    private String emailToUserIdKey(String normalizedEmail) {
        return "auth:user:email:" + normalizedEmail;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record LocalUserEntry(AuthUser userSnapshot, Instant expiresAt) {
    }

    private record LocalIndexEntry(String userId, Instant expiresAt) {
    }
}
