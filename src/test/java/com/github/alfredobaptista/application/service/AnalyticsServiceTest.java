package com.github.alfredobaptista.application.service;

import com.github.alfredobaptista.application.port.out.AnalyticsRepository;
import com.github.alfredobaptista.domain.model.UrlAnalytics;
import com.github.alfredobaptista.domain.valueobject.ShortCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RegisterRedirectAnalyticsServiceTest {

    @Mock
    private AnalyticsRepository analyticsRepository;

    @InjectMocks
    private RegisterRedirectAnalyticsService service;

    @Test
    void shouldRegisterRedirectSuccessfully() {
        // Arrange
        ShortCode shortCode = new ShortCode("abc123");

        ArgumentCaptor<UrlAnalytics> analyticsCaptor =
                ArgumentCaptor.forClass(UrlAnalytics.class);

        // Act
        service.registerRedirect(shortCode);

        // Assert
        verify(analyticsRepository).save(
                analyticsCaptor.capture()
        );

        UrlAnalytics analytics = analyticsCaptor.getValue();

        assertNotNull(analytics);
        assertEquals(shortCode, analytics.getShortCode());
        assertNotNull(analytics.getAccessedAt());
        assertNull(analytics.getId());
    }
}