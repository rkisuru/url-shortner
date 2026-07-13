package com.rkisuru.url_shortner.services;

import com.rkisuru.url_shortner.dto.CreateUrlRequest;
import com.rkisuru.url_shortner.dto.UrlResponse;
import com.rkisuru.url_shortner.entity.Url;
import com.rkisuru.url_shortner.exception.UrlNotFoundException;
import com.rkisuru.url_shortner.repository.UrlRepository;
import com.rkisuru.url_shortner.util.Base62Encoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository urlRepository;
    private final Base62Encoder base62Encoder;

    private static final String BASE_URL = "http://localhost:8080/";

    public UrlResponse createShortUrl(CreateUrlRequest request) {
        Url url = Url.builder()
                .shortCode("PENDING") // placeholder, replaced after we get an ID
                .longUrl(request.longUrl())
                .createdAt(LocalDateTime.now())
                .clickCount(0L)
                .build();

        // First save to get an auto-generated ID
        Url saved = urlRepository.save(url);

        // Now generate the real short code from that ID and update
        String shortCode = base62Encoder.encode(saved.getId());
        saved.setShortCode(shortCode);
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

        url.setClickCount(url.getClickCount() + 1);
        urlRepository.save(url);

        return url.getLongUrl();
    }
}
