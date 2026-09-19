package com.rkisuru.url_shortner.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response body for URL statistics, including analytics breakdown.
 */
public record UrlStatsResponse(
        String shortCode,
        String shortUrl,
        String longUrl,
        Long clickCount,
        LocalDateTime createdAt,
        LocalDateTime expiresAt,
        List<DailyClickCount> dailyClicks,
        List<ReferrerCount> topReferrers
) {
}
