package com.github.alfredobaptista.application.service;

import com.github.alfredobaptista.application.port.out.AnalyticsPublisher;
import com.github.alfredobaptista.application.port.out.UrlCache;
import com.github.alfredobaptista.application.port.out.UrlRepository;
import com.github.alfredobaptista.domain.exception.UrlExpiredException;
import com.github.alfredobaptista.domain.exception.UrlNotFoundException;
import com.github.alfredobaptista.domain.model.Url;
import com.github.alfredobaptista.domain.valueobject.OriginalUrl;
import com.github.alfredobaptista.domain.valueobject.ShortCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedirectUrlServiceTest {

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private UrlCache urlCache;

    @Mock
    private AnalyticsPublisher analyticsPublisher;

    @InjectMocks
    private RedirectUrlService service;

    @Test
    void shouldRedirectUsingCachedUrl() {

        // Arrange
        String shortCode = "abc123";
        String originalUrl = "https://example.com";

        when(urlCache.get(shortCode))
                .thenReturn(Optional.of(originalUrl));

        // Act
        String result = service.redirect(shortCode);

        // Assert
        assertEquals(originalUrl, result);

        verify(urlCache).get(shortCode);

        verify(analyticsPublisher)
                .publishRedirect(
                        eq(new ShortCode(shortCode))
                );

        verifyNoInteractions(urlRepository);
    }

    @Test
    void shouldLoadUrlFromRepositoryWhenCacheMissOccurs() {

        // Arrange
        String shortCode = "abc123";
        ShortCode code = new ShortCode(shortCode);

        Url url = new Url(
                code,
                new OriginalUrl("https://example.com"),
                null
        );

        when(urlCache.get(shortCode))
                .thenReturn(Optional.empty());

        when(urlRepository.findByShortCode(code))
                .thenReturn(Optional.of(url));

        // Act
        String result = service.redirect(shortCode);

        // Assert
        assertEquals(
                "https://example.com",
                result
        );

        verify(urlCache).get(shortCode);

        verify(urlRepository)
                .findByShortCode(code);

        verify(urlCache).save(
                eq(shortCode),
                eq("https://example.com"),
                any()
        );

        verify(analyticsPublisher)
                .publishRedirect(code);
    }

    @Test
    void shouldThrowExceptionWhenShortUrlDoesNotExist() {

        // Arrange
        String shortCode = "missing";

        when(urlCache.get(shortCode))
                .thenReturn(Optional.empty());

        when(urlRepository.findByShortCode(
                new ShortCode(shortCode)
        )).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                UrlNotFoundException.class,
                () -> service.redirect(shortCode)
        );

        verify(urlRepository)
                .findByShortCode(
                        new ShortCode(shortCode)
                );

        verifyNoInteractions(analyticsPublisher);
    }

    @Test
    void shouldThrowExceptionWhenUrlHasExpired()
            throws InterruptedException {

        // Arrange
        String shortCode = "exp123";

        LocalDateTime expiresAt =
                LocalDateTime.now().plusNanos(500_000_000);

        Url url = new Url(
                new ShortCode(shortCode),
                new OriginalUrl("https://example.com"),
                expiresAt
        );

        when(urlCache.get(shortCode))
                .thenReturn(Optional.empty());

        when(urlRepository.findByShortCode(
                new ShortCode(shortCode)
        )).thenReturn(Optional.of(url));

        // Aguarda a expiração da URL
        Thread.sleep(600);

        // Act + Assert
        assertThrows(
                UrlExpiredException.class,
                () -> service.redirect(shortCode)
        );

        verify(urlRepository)
                .findByShortCode(
                        new ShortCode(shortCode)
                );

        verifyNoInteractions(analyticsPublisher);

        verify(urlCache)
                .get(shortCode);
    }

    @Test
    void shouldContinueRedirectWhenAnalyticsPublishingFails() {

        // Arrange
        String shortCode = "abc123";

        when(urlCache.get(shortCode))
                .thenReturn(Optional.of("https://example.com"));

        doThrow(new RuntimeException("RabbitMQ unavailable"))
                .when(analyticsPublisher)
                .publishRedirect(any(ShortCode.class));

        // Act
        String result = service.redirect(shortCode);

        // Assert
        assertEquals(
                "https://example.com",
                result
        );

        verify(analyticsPublisher)
                .publishRedirect(
                        new ShortCode(shortCode)
                );
    }
}