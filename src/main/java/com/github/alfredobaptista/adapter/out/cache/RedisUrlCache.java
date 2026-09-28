package com.github.alfredobaptista.adapter.out.cache;

import com.github.alfredobaptista.application.port.out.UrlCache;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
public class RedisUrlCache implements UrlCache {

    private static final String KEY_PREFIX = "url:";

    private final StringRedisTemplate redisTemplate;

    public RedisUrlCache(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Optional<String> get(String shortCode) {

        String key = KEY_PREFIX + shortCode;

        return Optional.ofNullable(
                redisTemplate.opsForValue().get(key)
        );
    }

    @Override
    public void save(
            String shortCode,
            String originalUrl,
            Duration ttl
    ) {

        String key = KEY_PREFIX + shortCode;

        redisTemplate.opsForValue().set(
                key,
                originalUrl,
                ttl
        );
    }

    @Override
    public void delete(String shortCode) {

        String key = KEY_PREFIX + shortCode;

        redisTemplate.delete(key);
    }
}