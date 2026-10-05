package com.github.alfredobaptista.application.service;

import com.github.alfredobaptista.application.dto.UrlAnalyticsResult;
import com.github.alfredobaptista.application.port.out.AnalyticsRepository;
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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetUrlAnalyticsServiceTest {

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private AnalyticsRepository analyticsRepository;

    @InjectMocks
    private GetUrlAnalyticsService service;

    @Test
    void shouldReturnAnalyticsSuccessfully() {
        // Arrange
        String shortCode = "abc123";
        ShortCode code = new ShortCode(shortCode);

        Url url = new Url(
                code,
                new OriginalUrl("https://example.com"),
                null
        );

        LocalDateTime firstAccessedAt =
                LocalDateTime.of(2026, 9, 28, 8, 0);

        LocalDateTime lastAccessedAt =
                LocalDateTime.of(2026, 9, 28, 8, 20);

        when(urlRepository.findByShortCode(code))
                .thenReturn(Optional.of(url));

        when(analyticsRepository.countByShortCode(code))
                .thenReturn(15L);

        when(analyticsRepository.findFirstAccessedAt(code))
                .thenReturn(firstAccessedAt);

        when(analyticsRepository.findLastAccessedAt(code))
                .thenReturn(lastAccessedAt);

        // Act
        UrlAnalyticsResult result =
                service.getAnalytics(shortCode);

        // Assert
        assertNotNull(result);
        assertEquals(shortCode, result.shortCode());
        assertEquals(15L, result.totalClicks());
        assertEquals(firstAccessedAt, result.firstAccessedAt());
        assertEquals(lastAccessedAt, result.lastAccessedAt());

        verify(urlRepository).findByShortCode(code);

        verify(analyticsRepository)
                .countByShortCode(code);

        verify(analyticsRepository)
                .findFirstAccessedAt(code);

        verify(analyticsRepository)
                .findLastAccessedAt(code);
    }

    @Test
    void shouldReturnZeroClicksWhenThereAreNoRedirects() {
        // Arrange
        String shortCode = "abc123";
        ShortCode code = new ShortCode(shortCode);

        Url url = new Url(
                code,
                new OriginalUrl("https://example.com"),
                null
        );

        when(urlRepository.findByShortCode(code))
                .thenReturn(Optional.of(url));

        when(analyticsRepository.countByShortCode(code))
                .thenReturn(0L);

        when(analyticsRepository.findFirstAccessedAt(code))
                .thenReturn(null);

        when(analyticsRepository.findLastAccessedAt(code))
                .thenReturn(null);

        // Act
        UrlAnalyticsResult result =
                service.getAnalytics(shortCode);

        // Assert
        assertNotNull(result);
        assertEquals(shortCode, result.shortCode());
        assertEquals(0L, result.totalClicks());
        assertNull(result.firstAccessedAt());
        assertNull(result.lastAccessedAt());

        verify(urlRepository)
                .findByShortCode(code);

        verify(analyticsRepository)
                .countByShortCode(code);

        verify(analyticsRepository)
                .findFirstAccessedAt(code);

        verify(analyticsRepository)
                .findLastAccessedAt(code);
    }

    @Test
    void shouldThrowExceptionWhenUrlDoesNotExist() {
        // Arrange
        String shortCode = "abc123";
        ShortCode code = new ShortCode(shortCode);

        when(urlRepository.findByShortCode(code))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                UrlNotFoundException.class,
                () -> service.getAnalytics(shortCode)
        );

        verify(urlRepository)
                .findByShortCode(code);

        verifyNoInteractions(analyticsRepository);
    }

    @Test
    void shouldRejectInvalidShortCode() {
        // Arrange
        String shortCode = "abc-123";

        // Act + Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getAnalytics(shortCode)
        );

        verifyNoInteractions(urlRepository);
        verifyNoInteractions(analyticsRepository);
    }
}