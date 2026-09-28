package com.github.alfredobaptista.application.service;

import com.github.alfredobaptista.application.dto.UrlAnalyticsResult;
import com.github.alfredobaptista.application.port.in.GetUrlAnalyticsUseCase;
import com.github.alfredobaptista.application.port.out.AnalyticsRepository;
import com.github.alfredobaptista.domain.model.UrlAnalytics;
import com.github.alfredobaptista.domain.valueobject.ShortCode;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AnalyticsService
        implements GetUrlAnalyticsUseCase {

    private final AnalyticsRepository analyticsRepository;

    public AnalyticsService(
            AnalyticsRepository analyticsRepository
    ) {
        this.analyticsRepository = analyticsRepository;
    }

    public void registerRedirect(ShortCode shortCode) {

        UrlAnalytics analytics = new UrlAnalytics(
                shortCode,
                LocalDateTime.now()
        );

        analyticsRepository.save(analytics);
    }

    @Override
    public UrlAnalyticsResult getAnalytics(
            String shortCode
    ) {
        ShortCode code = new ShortCode(shortCode);

        long totalClicks =
                analyticsRepository.countByShortCode(code);

        LocalDateTime firstAccessedAt =
                analyticsRepository.findFirstAccessedAt(code);

        LocalDateTime lastAccessedAt =
                analyticsRepository.findLastAccessedAt(code);

        return new UrlAnalyticsResult(
                code.getValue(),
                totalClicks,
                firstAccessedAt,
                lastAccessedAt
        );
    }
}