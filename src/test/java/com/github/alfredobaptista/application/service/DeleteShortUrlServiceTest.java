package com.github.alfredobaptista.application.service;

import com.github.alfredobaptista.application.port.out.UrlCache;
import com.github.alfredobaptista.application.port.out.UrlRepository;
import com.github.alfredobaptista.domain.exception.UrlNotFoundException;
import com.github.alfredobaptista.domain.model.Url;
import com.github.alfredobaptista.domain.valueobject.OriginalUrl;
import com.github.alfredobaptista.domain.valueobject.ShortCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteShortUrlServiceTest {

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private UrlCache urlCache;

    @InjectMocks
    private DeleteShortUrlService service;

    @Test
    void shouldDeleteShortUrlFromRepositoryAndCache() {

        // Arrange
        String shortCode = "abc123";

        Url url = new Url(
                new ShortCode(shortCode),
                new OriginalUrl("https://example.com"),
                null
        );

        when(urlRepository.findByShortCode(
                new ShortCode(shortCode)
        )).thenReturn(Optional.of(url));

        // Act
        service.deleteShortUrl(shortCode);

        // Assert
        verify(urlRepository)
                .findByShortCode(
                        new ShortCode(shortCode)
                );

        verify(urlRepository)
                .deleteByShortCode(
                        new ShortCode(shortCode)
                );

        verify(urlCache)
                .delete(shortCode);
    }

    @Test
    void shouldThrowExceptionWhenShortUrlDoesNotExist() {

        // Arrange
        String shortCode = "missing";

        when(urlRepository.findByShortCode(
                new ShortCode(shortCode)
        )).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                UrlNotFoundException.class,
                () -> service.deleteShortUrl(shortCode)
        );

        verify(urlRepository)
                .findByShortCode(
                        new ShortCode(shortCode)
                );

        verify(urlRepository, never())
                .deleteByShortCode(any(ShortCode.class));

        verifyNoInteractions(urlCache);
    }
}