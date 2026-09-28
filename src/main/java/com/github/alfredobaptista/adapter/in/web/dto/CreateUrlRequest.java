package com.github.alfredobaptista.adapter.in.web.dto;

import java.time.LocalDateTime;

public record CreateUrlRequest(
        String originalUrl,
        LocalDateTime expiresAt
) {
}