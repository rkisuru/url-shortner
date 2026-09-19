package com.rkisuru.url_shortner.dto;

/**
 * Message payload published to RabbitMQ when a click event occurs.
 * Consumed asynchronously by {@link com.rkisuru.url_shortner.services.ClickEventConsumer}.
 *
 * {@code clickedAt} is an ISO-8601 string (e.g. "2026-09-19T21:00:00") so that
 * Jackson can serialize it without requiring the JavaTimeModule.
 */
public record ClickEventMessage(
        String shortCode,
        String referrer,
        String userAgent,
        String ipAddress,
        String clickedAt
) {
}
