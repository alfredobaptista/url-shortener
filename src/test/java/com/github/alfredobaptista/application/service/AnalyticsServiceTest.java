package com.github.alfredobaptista.application.service;

import com.github.alfredobaptista.application.dto.UrlAnalyticsResult;
import com.github.alfredobaptista.application.port.out.AnalyticsRepository;
import com.github.alfredobaptista.domain.model.UrlAnalytics;
import com.github.alfredobaptista.domain.valueobject.ShortCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private AnalyticsRepository analyticsRepository;

    @InjectMocks
    private AnalyticsService service;

    @Test
    void shouldRegisterRedirectSuccessfully() {
        // Arrange
        ShortCode shortCode = new ShortCode("abc123");
        ArgumentCaptor<UrlAnalytics> analyticsCaptor =
                ArgumentCaptor.forClass(UrlAnalytics.class);

        // Act
        service.registerRedirect(shortCode);

        // Assert
        verify(analyticsRepository).save(analyticsCaptor.capture());

        UrlAnalytics analytics = analyticsCaptor.getValue();

        assertNotNull(analytics);
        assertEquals(shortCode, analytics.getShortCode());
        assertNotNull(analytics.getAccessedAt());
        assertNull(analytics.getId());
    }

    @Test
    void shouldReturnAnalyticsSuccessfully() {
        // Arrange
        String shortCode = "abc123";
        ShortCode code = new ShortCode(shortCode);

        LocalDateTime firstAccessedAt =
                LocalDateTime.of(2026, 9, 28, 8, 0);

        LocalDateTime lastAccessedAt =
                LocalDateTime.of(2026, 9, 28, 8, 20);

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

        verify(analyticsRepository).countByShortCode(code);
        verify(analyticsRepository).findFirstAccessedAt(code);
        verify(analyticsRepository).findLastAccessedAt(code);
    }

    @Test
    void shouldReturnZeroClicksWhenThereAreNoRedirects() {
        // Arrange
        String shortCode = "abc123";
        ShortCode code = new ShortCode(shortCode);

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

        verify(analyticsRepository).countByShortCode(code);
        verify(analyticsRepository).findFirstAccessedAt(code);
        verify(analyticsRepository).findLastAccessedAt(code);
    }

    @Test
    void shouldRejectInvalidShortCodeWhenGettingAnalytics() {
        // Arrange
        String shortCode = "abc-123";

        // Act + Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getAnalytics(shortCode)
        );

        verifyNoInteractions(analyticsRepository);
    }
}