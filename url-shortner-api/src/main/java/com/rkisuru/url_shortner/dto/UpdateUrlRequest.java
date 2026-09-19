package com.rkisuru.url_shortner.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Request body for updating an existing shortened URL.
 * Both fields are optional — only provided fields will be updated.
 */
public record UpdateUrlRequest(
        @Size(max = 2048, message = "longUrl must not exceed 2048 characters")
        @Pattern(regexp = "^https?://.+", message = "longUrl must start with http:// or https://")
        String longUrl,

        LocalDateTime expiresAt
) {
}
