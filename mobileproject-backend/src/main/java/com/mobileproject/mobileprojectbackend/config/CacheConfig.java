package com.mobileproject.mobileprojectbackend.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

import java.time.Duration;
import java.util.Map;

/**
 * Cấu hình Redis cache cho ứng dụng.
 *
 * <p>Chiến lược cache:</p>
 * <ul>
 *   <li><b>Key serializer</b>: {@link StringRedisSerializer} - key là string dễ đọc</li>
 *   <li><b>Value serializer</b>: {@link JdkSerializationRedisSerializer} - serialize object Java mặc định</li>
 *   <li><b>Null values</b>: Không cache giá trị null (tránh cache poisoning)</li>
 *   <li><b>Default TTL</b>: 1 giờ cho tất cả các cache</li>
 * </ul>
 *
 * <p>Cache đặc biệt:</p>
 * <ul>
 *   <li>{@code placeById} - TTL 6 giờ (dữ liệu địa điểm ít thay đổi)</li>
 * </ul>
 *
 * <p>Sử dụng {@code @EnableCaching} để kích hoạt annotation {@code @Cacheable},
 * {@code @CacheEvict}, {@code @CachePut} trên các service.</p>
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Cấu hình mặc định cho Redis cache.
     * Key dùng String serializer, value dùng JDK serializer, TTL 1 giờ, không cache null.
     *
     * @return cấu hình cache mặc định
     */
    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new JdkSerializationRedisSerializer()))
                .disableCachingNullValues()
                .entryTtl(Duration.ofHours(1));
    }

    /**
     * Tạo CacheManager backed by Redis với cấu hình tùy chỉnh cho từng cache.
     *
     * @param redisConnectionFactory kết nối Redis
     * @param redisCacheConfiguration cấu hình cache mặc định
     * @return CacheManager quản lý tất cả các cache
     */
    @Bean
    public CacheManager cacheManager(
            RedisConnectionFactory redisConnectionFactory,
            RedisCacheConfiguration redisCacheConfiguration) {
        Map<String, RedisCacheConfiguration> cacheConfigurations = Map.of(
                "placeById",
                redisCacheConfiguration.entryTtl(Duration.ofHours(6)));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(redisCacheConfiguration)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
