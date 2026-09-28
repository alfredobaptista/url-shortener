package com.github.alfredobaptista.application.port.out;

import com.github.alfredobaptista.domain.model.UrlAnalytics;
import com.github.alfredobaptista.domain.valueobject.ShortCode;

import java.time.LocalDateTime;

public interface AnalyticsRepository {

    UrlAnalytics save(UrlAnalytics analytics);

    long countByShortCode(ShortCode shortCode);

    LocalDateTime findFirstAccessedAt(ShortCode shortCode);

    LocalDateTime findLastAccessedAt(ShortCode shortCode);
}