package com.rkisuru.url_shortner.services;

import com.rkisuru.url_shortner.dto.CreateUrlRequest;
import com.rkisuru.url_shortner.dto.UrlResponse;
import com.rkisuru.url_shortner.entity.Url;
import com.rkisuru.url_shortner.exception.DuplicateAliasException;
import com.rkisuru.url_shortner.exception.UrlNotFoundException;
import com.rkisuru.url_shortner.repository.UrlRepository;
import com.rkisuru.url_shortner.util.Base62Encoder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository urlRepository;
    private final Base62Encoder base62Encoder;

    @Value("${app.base-url}")
    private String BASE_URL = "http://localhost:8080/";

    public UrlResponse createShortUrl(CreateUrlRequest request) {
        if (request.customAlias() != null && !request.customAlias().isBlank()) {
            return createWithCustomAlias(request);
        }
        return createWithGeneratedCode(request);
    }

    private UrlResponse createWithCustomAlias(CreateUrlRequest request) {
        // Early, user-friendly check (not the sole protection)
        if (urlRepository.existsByShortCode(request.customAlias())) {
            throw new DuplicateAliasException(request.customAlias());
        }

        Url url = Url.builder()
                .shortCode(request.customAlias())
                .longUrl(request.longUrl())
                .createdAt(LocalDateTime.now())
                .clickCount(0L)
                .build();

        try {
            Url saved = urlRepository.saveAndFlush(url);
            return toResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            // This is the real protection — catches the race condition
            // the exists() check above couldn't fully prevent.
            throw new DuplicateAliasException(request.customAlias());
        }
    }

    private UrlResponse createWithGeneratedCode(CreateUrlRequest request) {
        Url url = Url.builder()
                .shortCode("PENDING")
                .longUrl(request.longUrl())
                .createdAt(LocalDateTime.now())
                .clickCount(0L)
                .build();

        Url saved = urlRepository.save(url);
        saved.setShortCode(base62Encoder.encode(saved.getId()));
        Url updated = urlRepository.save(saved);

        return toResponse(updated);
    }

    private UrlResponse toResponse(Url url) {
        return new UrlResponse(
                url.getShortCode(),
                BASE_URL + url.getShortCode(),
                url.getLongUrl(),
                url.getCreatedAt(),
                url.getExpiresAt()
        );
    }

    public String resolveLongUrl(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));

        if (url.getExpiresAt() != null && url.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UrlNotFoundException(shortCode);
        }

        urlRepository.incrementClickCount(shortCode);
        urlRepository.save(url);

        return url.getLongUrl();
    }
}
