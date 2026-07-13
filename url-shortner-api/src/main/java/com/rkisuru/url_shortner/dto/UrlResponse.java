package com.rkisuru.url_shortner.dto;

import java.time.LocalDateTime;

public record UrlResponse(
        String shortCode,
        String shortUrl,
        String longUrl,
        LocalDateTime createdAt,
        LocalDateTime expiresAt
) {
}
