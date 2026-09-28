package com.github.alfredobaptista.adapter.out.security;

import com.github.alfredobaptista.application.port.out.AbuseChecker;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;

@Component
public class AbuseProtectionAdapter implements AbuseChecker {

    private static final String RATE_LIMIT_PREFIX = "rate:limit:";
    private static final int MAX_REQUESTS = 10;
    private static final Duration WINDOW_DURATION =
            Duration.ofMinutes(1);

    private static final String RATE_LIMIT_SCRIPT = """
            local count = redis.call('INCR', KEYS[1])

            if count == 1 then
                redis.call('EXPIRE', KEYS[1], ARGV[1])
            end

            if count <= tonumber(ARGV[2]) then
                return 1
            end

            return 0
            """;

    private final StringRedisTemplate redisTemplate;

    private final DefaultRedisScript<Long> rateLimitScript =
            new DefaultRedisScript<>(
                    RATE_LIMIT_SCRIPT,
                    Long.class
            );

    public AbuseProtectionAdapter(
            StringRedisTemplate redisTemplate
    ) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean isAllowed(String clientKey) {

        String key = RATE_LIMIT_PREFIX + clientKey;

        Long result = redisTemplate.execute(
                rateLimitScript,
                Collections.singletonList(key),
                String.valueOf(WINDOW_DURATION.toSeconds()),
                String.valueOf(MAX_REQUESTS)
        );

        return result != null && result == 1L;
    }
}