package com.github.alfredobaptista.application.dto;

import java.time.LocalDateTime;

public record UrlAnalyticsResult(
        String shortCode,
        long totalClicks,
        LocalDateTime firstAccessedAt,
        LocalDateTime lastAccessedAt
) {
}