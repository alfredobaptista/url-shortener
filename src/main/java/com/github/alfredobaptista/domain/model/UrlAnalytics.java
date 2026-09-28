package com.github.alfredobaptista.domain.model;

import com.github.alfredobaptista.domain.valueobject.ShortCode;

import java.time.LocalDateTime;
import java.util.Objects;

public class UrlAnalytics {

    private final Long id;
    private final ShortCode shortCode;
    private final LocalDateTime accessedAt;

    public UrlAnalytics(
            Long id,
            ShortCode shortCode,
            LocalDateTime accessedAt
    ) {
        this.id = id;
        this.shortCode = Objects.requireNonNull(
                shortCode,
                "O código curto não pode ser nulo."
        );
        this.accessedAt = Objects.requireNonNull(
                accessedAt,
                "A data de acesso não pode ser nula."
        );
    }

    public UrlAnalytics(
            ShortCode shortCode,
            LocalDateTime accessedAt
    ) {
        this(null, shortCode, accessedAt);
    }

    public Long getId() {
        return id;
    }

    public ShortCode getShortCode() {
        return shortCode;
    }

    public LocalDateTime getAccessedAt() {
        return accessedAt;
    }
}