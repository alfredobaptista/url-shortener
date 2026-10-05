package com.github.alfredobaptista.application.service;

import com.github.alfredobaptista.application.port.in.RegisterRedirectAnalyticsUseCase;
import com.github.alfredobaptista.application.port.out.AnalyticsRepository;
import com.github.alfredobaptista.domain.model.UrlAnalytics;
import com.github.alfredobaptista.domain.valueobject.ShortCode;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RegisterRedirectAnalyticsService
        implements RegisterRedirectAnalyticsUseCase {

    private final AnalyticsRepository analyticsRepository;

    public RegisterRedirectAnalyticsService(
            AnalyticsRepository analyticsRepository
    ) {
        this.analyticsRepository = analyticsRepository;
    }

    @Override
    public void registerRedirect(ShortCode shortCode) {

        UrlAnalytics analytics = new UrlAnalytics(
                shortCode,
                LocalDateTime.now()
        );

        analyticsRepository.save(analytics);
    }
}