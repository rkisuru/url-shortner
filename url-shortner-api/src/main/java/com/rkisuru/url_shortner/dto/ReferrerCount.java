package com.rkisuru.url_shortner.dto;

/**
 * Single entry in the top-referrers list.
 */
public record ReferrerCount(
        String referrer,
        Long count
) {
}
