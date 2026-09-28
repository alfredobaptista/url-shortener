package com.github.alfredobaptista.domain.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;

public final class ShortCode {

    private static final int MAX_LENGTH = 10;
    private static final Pattern VALID_PATTERN =
            Pattern.compile("^[a-zA-Z0-9]+$");

    private final String value;

    public ShortCode(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "O código curto não pode ser nulo ou vazio."
            );
        }

        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "O código curto não pode ter mais de " + MAX_LENGTH + " caracteres."
            );
        }

        if (!VALID_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    "O código curto deve conter apenas letras e números."
            );
        }

        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ShortCode other)) {
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