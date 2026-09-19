package com.rkisuru.url_shortner.services;

import com.rkisuru.url_shortner.dto.*;
import com.rkisuru.url_shortner.entity.Url;
import com.rkisuru.url_shortner.exception.DuplicateAliasException;
import com.rkisuru.url_shortner.exception.UrlNotFoundException;
import com.rkisuru.url_shortner.repository.ClickEventRepository;
import com.rkisuru.url_shortner.repository.UrlRepository;
import com.rkisuru.url_shortner.util.Base62Encoder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UrlService {

    @Value("${app.cache.url-ttl-seconds}")
    private long cacheTtlSeconds;

    private static final String CACHE_KEY_PREFIX = "url:shortcode:";

    private final UrlRepository urlRepository;
    private final ClickEventRepository clickEventRepository;
    private final Base62Encoder base62Encoder;
    private final RedisTemplate<String, String> redisTemplate;
    private final ClickEventPublisher clickEventPublisher;

    @Value("${app.base-url}")
    private String baseUrl;

    // ─── Create ─────────────────────────────────────

    public UrlResponse createShortUrl(CreateUrlRequest request, String userId) {
        if (request.customAlias() != null && !request.customAlias().isBlank()) {
            return createWithCustomAlias(request, userId);
        }
        return createWithGeneratedCode(request, userId);
    }

    private UrlResponse createWithCustomAlias(CreateUrlRequest request, String userId) {
        // Early, user-friendly check (not the sole protection)
        if (urlRepository.existsByShortCode(request.customAlias())) {
            throw new DuplicateAliasException(request.customAlias());
        }

        Url url = Url.builder()
                .shortCode(request.customAlias())
                .longUrl(request.longUrl())
                .ownerUserId(userId)
                .expiresAt(request.expiresAt())
                .build();

        try {
            Url saved = urlRepository.saveAndFlush(url);
            return toResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            // Real protection — catches the race condition
            throw new DuplicateAliasException(request.customAlias());
        }
    }

    private UrlResponse createWithGeneratedCode(CreateUrlRequest request, String userId) {
        Url url = Url.builder()
                .shortCode("PENDING")
                .longUrl(request.longUrl())
                .ownerUserId(userId)
                .expiresAt(request.expiresAt())
                .build();

        Url saved = urlRepository.save(url);
        saved.setShortCode(base62Encoder.encode(saved.getId()));
        Url updated = urlRepository.save(saved);

        return toResponse(updated);
    }

    // ─── Read ───────────────────────────────────────

    @Transactional(readOnly = true)
    public UrlResponse getUrlByCode(String shortCode, String userId) {
        Url url = urlRepository.findByShortCodeAndOwnerUserId(shortCode, userId)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));
        return toResponse(url);
    }

    @Transactional(readOnly = true)
    public List<UrlResponse> getAllUrlsByUser(String userId) {
        return urlRepository.findAllByOwnerUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    // ─── Update ─────────────────────────────────────

    public UrlResponse updateUrl(String shortCode, UpdateUrlRequest request, String userId) {
        Url url = urlRepository.findByShortCodeAndOwnerUserId(shortCode, userId)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));

        if (request.longUrl() != null && !request.longUrl().isBlank()) {
            url.setLongUrl(request.longUrl());
            // Evict stale cache entry
            redisTemplate.delete(CACHE_KEY_PREFIX + shortCode);
        }

        if (request.expiresAt() != null) {
            url.setExpiresAt(request.expiresAt());
        }

        Url updated = urlRepository.save(url);
        return toResponse(updated);
    }

    // ─── Delete ─────────────────────────────────────

    public void deleteUrl(String shortCode, String userId) {
        int deleted = urlRepository.deleteByShortCodeAndOwnerUserId(shortCode, userId);
        if (deleted == 0) {
            throw new UrlNotFoundException(shortCode);
        }
        redisTemplate.delete(CACHE_KEY_PREFIX + shortCode);
    }

    // ─── Redirect (public) ──────────────────────────

    /**
     * Resolves the long URL for a given short code.
     * Click recording is done asynchronously via RabbitMQ — the redirect
     * response is returned immediately without waiting for analytics persistence.
     */
    public String resolveLongUrl(String shortCode, String referrer, String userAgent, String ipAddress) {
        String cacheKey = CACHE_KEY_PREFIX + shortCode;

        // 1. Check cache first
        String cachedUrl = redisTemplate.opsForValue().get(cacheKey);
        if (cachedUrl != null) {
            // Fire-and-forget: publish click event to RabbitMQ
            clickEventPublisher.publish(shortCode, referrer, userAgent, ipAddress);
            return cachedUrl;
        }

        // 2. Cache miss — fall back to Postgres
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));

        if (url.getExpiresAt() != null && url.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UrlNotFoundException(shortCode);
        }

        // 3. Populate cache for next time
        redisTemplate.opsForValue().set(cacheKey, url.getLongUrl(), Duration.ofSeconds(cacheTtlSeconds));

        // Fire-and-forget: publish click event to RabbitMQ
        clickEventPublisher.publish(shortCode, referrer, userAgent, ipAddress);

        return url.getLongUrl();
    }

    // ─── Stats ──────────────────────────────────────

    @Transactional(readOnly = true)
    public UrlStatsResponse getUrlStats(String shortCode, String userId) {
        Url url = urlRepository.findByShortCodeAndOwnerUserId(shortCode, userId)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));

        // Daily click breakdown (last 30 days)
        List<DailyClickCount> dailyClicks = clickEventRepository
                .findDailyClickBreakdown(shortCode)
                .stream()
                .map(row -> new DailyClickCount(
                        row[0] instanceof java.time.LocalDate ld
                                ? ld
                                : ((java.sql.Date) row[0]).toLocalDate(),
                        ((Number) row[1]).longValue()
                ))
                .toList();

        // Top 10 referrers
        List<ReferrerCount> topReferrers = clickEventRepository
                .findTopReferrers(shortCode, 10)
                .stream()
                .map(row -> new ReferrerCount(
                        (String) row[0],
                        ((Number) row[1]).longValue()
                ))
                .toList();

        return new UrlStatsResponse(
                url.getShortCode(),
                baseUrl + url.getShortCode(),
                url.getLongUrl(),
                url.getClickCount(),
                url.getCreatedAt(),
                url.getExpiresAt(),
                dailyClicks,
                topReferrers
        );
    }

    // ─── Helpers ────────────────────────────────────

    private UrlResponse toResponse(Url url) {
        return new UrlResponse(
                url.getId(),
                url.getShortCode(),
                baseUrl + url.getShortCode(),
                url.getLongUrl(),
                url.getOwnerUserId(),
                url.getCreatedAt(),
                url.getExpiresAt(),
                url.getClickCount()
        );
    }
}
