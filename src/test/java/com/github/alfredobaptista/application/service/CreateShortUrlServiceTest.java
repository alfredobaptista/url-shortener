package com.github.alfredobaptista.application.service;

import com.github.alfredobaptista.application.dto.CreateShortUrlCommand;
import com.github.alfredobaptista.application.dto.ShortUrlResult;
import com.github.alfredobaptista.application.port.out.AbuseChecker;
import com.github.alfredobaptista.application.port.out.ShortCodeGenerator;
import com.github.alfredobaptista.application.port.out.UrlCache;
import com.github.alfredobaptista.application.port.out.UrlRepository;
import com.github.alfredobaptista.domain.exception.RateLimitExceededException;
import com.github.alfredobaptista.domain.model.Url;
import com.github.alfredobaptista.domain.valueobject.ShortCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateShortUrlServiceTest {

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private UrlCache urlCache;

    @Mock
    private ShortCodeGenerator shortCodeGenerator;

    @Mock
    private AbuseChecker abuseChecker;

    @InjectMocks
    private CreateShortUrlService service;

    @Test
    void shouldCreateShortUrlSuccessfully() {

        // Arrange
        CreateShortUrlCommand command = new CreateShortUrlCommand(
                "https://example.com",
                null,
                "127.0.0.1"
        );

        ShortCode shortCode = new ShortCode("abc123");

        when(abuseChecker.isAllowed("127.0.0.1"))
                .thenReturn(true);

        when(shortCodeGenerator.generate())
                .thenReturn(shortCode);

        // Act
        ShortUrlResult result = service.createShortUrl(command);

        // Assert
        assertNotNull(result);
        assertEquals("abc123", result.shortCode());

        verify(abuseChecker).isAllowed("127.0.0.1");
        verify(shortCodeGenerator).generate();
        verify(urlRepository).save(any(Url.class));

        verify(urlCache).save(
                eq("abc123"),
                eq("https://example.com"),
                any(Duration.class)
        );
    }

    @Test
    void shouldRejectRequestWhenRateLimitIsExceeded() {

        // Arrange
        CreateShortUrlCommand command = new CreateShortUrlCommand(
                "https://example.com",
                null,
                "127.0.0.1"
        );

        when(abuseChecker.isAllowed("127.0.0.1"))
                .thenReturn(false);

        // Act + Assert
        assertThrows(
                RateLimitExceededException.class,
                () -> service.createShortUrl(command)
        );

        verify(abuseChecker).isAllowed("127.0.0.1");

        verifyNoInteractions(
                shortCodeGenerator,
                urlRepository,
                urlCache
        );
    }

    @Test
    void shouldPersistUrlWithGeneratedShortCode() {

        // Arrange
        CreateShortUrlCommand command = new CreateShortUrlCommand(
                "https://example.com",
                null,
                "127.0.0.1"
        );

        ShortCode shortCode = new ShortCode("xyz789");

        when(abuseChecker.isAllowed("127.0.0.1"))
                .thenReturn(true);

        when(shortCodeGenerator.generate())
                .thenReturn(shortCode);

        ArgumentCaptor<Url> urlCaptor =
                ArgumentCaptor.forClass(Url.class);

        // Act
        service.createShortUrl(command);

        // Assert
        verify(urlRepository).save(urlCaptor.capture());

        Url savedUrl = urlCaptor.getValue();

        assertEquals(
                "xyz789",
                savedUrl.getShortCode().getValue()
        );

        assertEquals(
                "https://example.com",
                savedUrl.getOriginalUrl().getValue()
        );

        assertNull(savedUrl.getExpiresAt());
    }

    @Test
    void shouldRespectExpirationWhenCachingUrl() {

        // Arrange
        LocalDateTime expiresAt =
                LocalDateTime.now().plusHours(2);

        CreateShortUrlCommand command = new CreateShortUrlCommand(
                "https://example.com",
                expiresAt,
                "127.0.0.1"
        );

        ShortCode shortCode = new ShortCode("exp123");

        when(abuseChecker.isAllowed("127.0.0.1"))
                .thenReturn(true);

        when(shortCodeGenerator.generate())
                .thenReturn(shortCode);

        // Act
        service.createShortUrl(command);

        // Assert
        verify(urlCache).save(
                eq("exp123"),
                eq("https://example.com"),
                argThat(ttl ->
                        ttl.compareTo(Duration.ZERO) > 0 &&
                        ttl.compareTo(Duration.ofHours(2)) <= 0
                )
        );
    }
}