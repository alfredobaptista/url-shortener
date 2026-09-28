package com.github.alfredobaptista.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OriginalUrlTest {

    @Test
    void shouldCreateValidHttpUrl() {
        // Arrange
        String value = "http://example.com";

        // Act
        OriginalUrl originalUrl = new OriginalUrl(value);

        // Assert
        assertEquals(value, originalUrl.getValue());
    }

    @Test
    void shouldCreateValidHttpsUrl() {
        // Arrange
        String value = "https://example.com";

        // Act
        OriginalUrl originalUrl = new OriginalUrl(value);

        // Assert
        assertEquals(value, originalUrl.getValue());
    }

    @Test
    void shouldTrimUrlBeforeStoring() {
        // Arrange
        String value = "  https://example.com  ";

        // Act
        OriginalUrl originalUrl = new OriginalUrl(value);

        // Assert
        assertEquals("https://example.com", originalUrl.getValue());
    }

    @Test
    void shouldRejectNullUrl() {
        // Arrange
        String value = null;

        // Act + Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new OriginalUrl(value)
        );

        assertEquals(
                "A URL original não pode ser nula ou vazia.",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectBlankUrl() {
        // Arrange
        String value = "   ";

        // Act + Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new OriginalUrl(value)
        );

        assertEquals(
                "A URL original não pode ser nula ou vazia.",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectUrlWithInvalidProtocol() {
        // Arrange
        String value = "ftp://example.com";

        // Act + Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new OriginalUrl(value)
        );

        assertEquals(
                "A URL original informada é inválida.",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectUrlWithoutHost() {
        // Arrange
        String value = "https:///path";

        // Act + Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new OriginalUrl(value)
        );

        assertEquals(
                "A URL original informada é inválida.",
                exception.getMessage()
        );
    }

    @Test
    void shouldConsiderEqualValuesAsEqual() {
        // Arrange
        OriginalUrl first = new OriginalUrl("https://example.com");
        OriginalUrl second = new OriginalUrl("https://example.com");

        // Act
        boolean result = first.equals(second);

        // Assert
        assertTrue(result);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void shouldReturnValueWhenToStringIsCalled() {
        // Arrange
        OriginalUrl originalUrl = new OriginalUrl("https://example.com");

        // Act
        String result = originalUrl.toString();

        // Assert
        assertEquals("https://example.com", result);
    }
}