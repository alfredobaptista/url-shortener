package com.github.alfredobaptista.domain.valueobject;

import java.net.URI;
import java.util.Objects;

public final class OriginalUrl {

    private final String value;

    public OriginalUrl(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "A URL original não pode ser nula ou vazia."
            );
        }

        String normalizedValue = value.trim();

        if (!isValidUrl(normalizedValue)) {
            throw new IllegalArgumentException(
                    "A URL original informada é inválida."
            );
        }

        this.value = normalizedValue;
    }

    private static boolean isValidUrl(String url) {
        try {
            URI uri = URI.create(url);

            return ("http".equalsIgnoreCase(uri.getScheme())
                    || "https".equalsIgnoreCase(uri.getScheme()))
                    && uri.getHost() != null;

        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof OriginalUrl other)) {
            return false;
        }

        return value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}