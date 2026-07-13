package com.rkisuru.url_shortner.exception;

public class UrlNotFoundException extends RuntimeException {
    public UrlNotFoundException(String shortCode) {
        super("No active URL found for short code: " + shortCode);
    }
}
