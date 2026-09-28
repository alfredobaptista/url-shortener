package com.github.alfredobaptista.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShortCodeTest {

    @Test
    void shouldCreateValidShortCode() {
        // Arrange
        String value = "abc123";

        // Act
        ShortCode shortCode = new ShortCode(value);

        // Assert
        assertEquals(value, shortCode.getValue());
    }

    @Test
    void shouldRejectNullShortCode() {
        // Arrange
        String value = null;

        // Act + Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ShortCode(value)
        );

        assertEquals(
                "O código curto não pode ser nulo ou vazio.",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectBlankShortCode() {
        // Arrange
        String value = "   ";

        // Act + Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ShortCode(value)
        );

        assertEquals(
                "O código curto não pode ser nulo ou vazio.",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectShortCodeLongerThanTenCharacters() {
        // Arrange
        String value = "abcdefghijk";

        // Act + Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ShortCode(value)
        );

        assertEquals(
                "O código curto não pode ter mais de 10 caracteres.",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectShortCodeWithInvalidCharacters() {
        // Arrange
        String value = "abc-123";

        // Act + Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ShortCode(value)
        );

        assertEquals(
                "O código curto deve conter apenas letras e números.",
                exception.getMessage()
        );
    }

    @Test
    void shouldConsiderEqualValuesAsEqual() {
        // Arrange
        ShortCode first = new ShortCode("abc123");
        ShortCode second = new ShortCode("abc123");

        // Act
        boolean result = first.equals(second);

        // Assert
        assertTrue(result);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void shouldReturnValueWhenToStringIsCalled() {
        // Arrange
        ShortCode shortCode = new ShortCode("abc123");

        // Act
        String result = shortCode.toString();

        // Assert
        assertEquals("abc123", result);
    }
}