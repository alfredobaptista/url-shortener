package com.github.alfredobaptista.domain.model;

import com.github.alfredobaptista.domain.valueobject.OriginalUrl;
import com.github.alfredobaptista.domain.valueobject.ShortCode;

import java.time.LocalDateTime;
import java.util.Objects;

public class Url {

    private final Long id;
    private final ShortCode shortCode;
    private final OriginalUrl originalUrl;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiresAt;

    public Url(
            Long id,
            ShortCode shortCode,
            OriginalUrl originalUrl,
            LocalDateTime createdAt,
            LocalDateTime expiresAt
    ) {
        this.id = id;

        this.shortCode = Objects.requireNonNull(
                shortCode,
                "O código curto não pode ser nulo."
        );

        this.originalUrl = Objects.requireNonNull(
                originalUrl,
                "A URL original não pode ser nula."
        );

        this.createdAt = createdAt != null
                ? createdAt
                : LocalDateTime.now();

        if (expiresAt != null && expiresAt.isBefore(this.createdAt)) {
            throw new IllegalArgumentException(
                    "A data de expiração não pode ser anterior à data de criação."
            );
        }

        this.expiresAt = expiresAt;
    }

    /**
     * Cria uma nova URL antes da persistência.
     */
    public Url(
            ShortCode shortCode,
            OriginalUrl originalUrl,
            LocalDateTime expiresAt
    ) {
        this(
                null,
                shortCode,
                originalUrl,
                LocalDateTime.now(),
                expiresAt
        );
    }

    public boolean isExpired() {
        if (expiresAt == null) {
            return false;
        }

        return !LocalDateTime.now().isBefore(expiresAt);
    }

    public Long getId() {
        return id;
    }

    public ShortCode getShortCode() {
        return shortCode;
    }

    public OriginalUrl getOriginalUrl() {
        return originalUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
}