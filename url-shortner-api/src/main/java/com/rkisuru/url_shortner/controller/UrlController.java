package com.rkisuru.url_shortner.controller;

import com.rkisuru.url_shortner.dto.*;
import com.rkisuru.url_shortner.services.UrlService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;

    /**
     * Create a new short URL (authenticated).
     * userId is extracted from the JWT by the JwtAuthenticationFilter.
     */
    @PostMapping("/api/urls")
    public ResponseEntity<UrlResponse> createShortUrl(
            HttpServletRequest request,
            @Valid @RequestBody CreateUrlRequest createRequest) {
        String userId = (String) request.getAttribute("userId");
        UrlResponse response = urlService.createShortUrl(createRequest, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Redirect — public endpoint, no authentication required.
     * Captures referrer, user-agent, and IP for click analytics.
     */
    @GetMapping("/api/r/{shortCode}")
    public ResponseEntity<Void> redirect(
            @PathVariable String shortCode,
            HttpServletRequest request) {
        String referrer = request.getHeader("Referer");
        String userAgent = request.getHeader("User-Agent");
        String ipAddress = request.getRemoteAddr();

        String longUrl = urlService.resolveLongUrl(shortCode, referrer, userAgent, ipAddress);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", longUrl)
                .build();
    }

    /**
     * Get a single URL by short code (authenticated, owner only).
     */
    @GetMapping("/api/urls/{shortCode}")
    public ResponseEntity<UrlResponse> getUrl(
            HttpServletRequest request,
            @PathVariable String shortCode) {
        String userId = (String) request.getAttribute("userId");
        UrlResponse response = urlService.getUrlByCode(shortCode, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all URLs for the authenticated user.
     */
    @GetMapping("/api/urls")
    public ResponseEntity<List<UrlResponse>> getAllUrls(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        List<UrlResponse> urls = urlService.getAllUrlsByUser(userId);
        return ResponseEntity.ok(urls);
    }

    /**
     * Update a URL (authenticated, owner only).
     */
    @PutMapping("/api/urls/{shortCode}")
    public ResponseEntity<UrlResponse> updateUrl(
            HttpServletRequest request,
            @PathVariable String shortCode,
            @Valid @RequestBody UpdateUrlRequest updateRequest) {
        String userId = (String) request.getAttribute("userId");
        UrlResponse response = urlService.updateUrl(shortCode, updateRequest, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a URL (authenticated, owner only).
     */
    @DeleteMapping("/api/urls/{shortCode}")
    public ResponseEntity<Void> deleteUrl(
            HttpServletRequest request,
            @PathVariable String shortCode) {
        String userId = (String) request.getAttribute("userId");
        urlService.deleteUrl(shortCode, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get URL statistics with analytics (authenticated, owner only).
     */
    @GetMapping("/api/urls/{shortCode}/stats")
    public ResponseEntity<UrlStatsResponse> getUrlStats(
            HttpServletRequest request,
            @PathVariable String shortCode) {
        String userId = (String) request.getAttribute("userId");
        UrlStatsResponse stats = urlService.getUrlStats(shortCode, userId);
        return ResponseEntity.ok(stats);
    }
}
