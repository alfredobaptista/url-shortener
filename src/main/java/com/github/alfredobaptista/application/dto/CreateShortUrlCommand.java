package com.github.alfredobaptista.application.dto;

import java.time.LocalDateTime;

public record CreateShortUrlCommand(
        String originalUrl,
        LocalDateTime expiresAt,
        String clientKey
) {
}