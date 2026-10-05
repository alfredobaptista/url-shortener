package com.github.alfredobaptista.application.service;

import com.github.alfredobaptista.application.dto.UrlAnalyticsResult;
import com.github.alfredobaptista.application.port.in.GetUrlAnalyticsUseCase;
import com.github.alfredobaptista.application.port.out.AnalyticsRepository;
import com.github.alfredobaptista.application.port.out.UrlRepository;
import com.github.alfredobaptista.domain.exception.UrlNotFoundException;
import com.github.alfredobaptista.domain.valueobject.ShortCode;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class GetUrlAnalyticsService
        implements GetUrlAnalyticsUseCase {

    private final UrlRepository urlRepository;
    private final AnalyticsRepository analyticsRepository;

    public GetUrlAnalyticsService(
            UrlRepository urlRepository,
            AnalyticsRepository analyticsRepository
    ) {
        this.urlRepository = urlRepository;
        this.analyticsRepository = analyticsRepository;
    }

    @Override
    public UrlAnalyticsResult getAnalytics(String shortCode) {

        ShortCode code = new ShortCode(shortCode);

        urlRepository.findByShortCode(code)
                .orElseThrow(() ->
                        new UrlNotFoundException(
                                "URL curta não encontrada: " + shortCode
                        )
                );

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