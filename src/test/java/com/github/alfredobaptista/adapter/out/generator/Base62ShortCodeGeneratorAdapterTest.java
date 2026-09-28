package com.github.alfredobaptista.adapter.out.generator;

import com.github.alfredobaptista.domain.valueobject.ShortCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Base62ShortCodeGeneratorAdapterTest {

    private final Base62ShortCodeGeneratorAdapter generator =
            new Base62ShortCodeGeneratorAdapter();

    @Test
    void shouldGenerateShortCodeWithSixCharacters() {
        // Arrange
        // O generator não necessita de preparação adicional.

        // Act
        ShortCode result = generator.generate();

        // Assert
        assertNotNull(result);
        assertEquals(6, result.getValue().length());
    }

    @Test
    void shouldGenerateShortCodeContainingOnlyBase62Characters() {
        // Arrange
        String base62Pattern = "^[a-zA-Z0-9]+$";

        // Act
        ShortCode result = generator.generate();

        // Assert
        assertTrue(result.getValue().matches(base62Pattern));
    }

    @Test
    void shouldGenerateDifferentShortCodesAcrossMultipleCalls() {
        // Arrange
        int numberOfCodes = 100;

        // Act
        var codes = java.util.stream.IntStream.range(0, numberOfCodes)
                .mapToObj(i -> generator.generate().getValue())
                .collect(java.util.stream.Collectors.toSet());

        // Assert
        assertTrue(codes.size() > 1);
    }
}