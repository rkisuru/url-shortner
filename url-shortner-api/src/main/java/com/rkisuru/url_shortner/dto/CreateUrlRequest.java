package com.rkisuru.url_shortner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUrlRequest (
        @NotBlank(message = "longUrl must not be blank")
        @Size(max = 2048, message = "longUrl must not exceed 2048 characters")
        @Pattern(regexp = "^https?://.+", message = "longUrl must start with http:// or https://")
        String longUrl,

        @Pattern(regexp = "^[a-zA-Z0-9_-]{3,20}$", message = "customAlias must be 3-20 alphanumeric characters, hyphens, or underscores")
        String customAlias // nullable — optional field
) {
}