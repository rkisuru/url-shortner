package com.rkisuru.url_shortner.dto;

import java.time.LocalDateTime;

public record UrlResponse(
        Long id,
        String shortCode,
        String shortUrl,
        String longUrl,
        String ownerUserId,
        LocalDateTime createdAt,
        LocalDateTime expiresAt,
        Long clickCount
) {
}
