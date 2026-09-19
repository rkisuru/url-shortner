package com.rkisuru.url_shortner.services;

import com.rkisuru.url_shortner.config.RabbitMQConfig;
import com.rkisuru.url_shortner.dto.ClickEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Publishes click events to RabbitMQ for async processing.
 * This decouples the redirect hot path from the analytics write path.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ClickEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(String shortCode, String referrer, String userAgent, String ipAddress) {
        ClickEventMessage message = new ClickEventMessage(
                shortCode, referrer, userAgent, ipAddress,
                LocalDateTime.now().toString()
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                message
        );

        log.debug("Published click event for shortCode={}", shortCode);
    }
}

