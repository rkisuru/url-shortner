package com.rkisuru.url_shortner.ratelimit;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class RateLimiter {

    private final RedisTemplate<String, String> redisTemplate;

    private static final int MAX_REQUESTS = 10;
    private static final Duration WINDOW = Duration.ofSeconds(60);

    /**
     * Sliding window rate limiter using a Redis sorted set:
     * each request is stored with its timestamp as the score,
     * old entries outside the window are trimmed on every check.
     */
    public boolean isAllowed(String clientIp) {
        String key = "ratelimit:" + clientIp;
        long now = Instant.now().toEpochMilli();
        long windowStart = now - WINDOW.toMillis();

        redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);

        Long currentCount = redisTemplate.opsForZSet().zCard(key);
        if (currentCount != null && currentCount >= MAX_REQUESTS) {
            return false;
        }

        redisTemplate.opsForZSet().add(key, String.valueOf(now), now);
        redisTemplate.expire(key, WINDOW);

        return true;
    }
}
