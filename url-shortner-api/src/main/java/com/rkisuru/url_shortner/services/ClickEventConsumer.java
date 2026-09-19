package com.rkisuru.url_shortner.services;

import com.rkisuru.url_shortner.config.RabbitMQConfig;
import com.rkisuru.url_shortner.dto.ClickEventMessage;
import com.rkisuru.url_shortner.entity.ClickEvent;
import com.rkisuru.url_shortner.repository.ClickEventRepository;
import com.rkisuru.url_shortner.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Consumes click events from RabbitMQ and persists them to Postgres.
 * This runs asynchronously — the redirect response is not blocked.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ClickEventConsumer {

    private final ClickEventRepository clickEventRepository;
    private final UrlRepository urlRepository;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    @Transactional
    public void handleClickEvent(ClickEventMessage message) {
        log.debug("Consuming click event for shortCode={}", message.shortCode());

        // 1. Increment aggregate counter
        urlRepository.incrementClickCount(message.shortCode());

        // 2. Persist detailed click event
        ClickEvent event = ClickEvent.builder()
                .shortCode(message.shortCode())
                .clickedAt(LocalDateTime.parse(message.clickedAt()))
                .referrer(message.referrer())
                .userAgent(message.userAgent())
                .ipAddress(message.ipAddress())
                .build();

        clickEventRepository.save(event);
    }
}

