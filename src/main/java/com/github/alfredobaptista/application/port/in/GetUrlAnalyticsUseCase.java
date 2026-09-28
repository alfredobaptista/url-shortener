package com.github.alfredobaptista.application.port.in;

import com.github.alfredobaptista.application.dto.UrlAnalyticsResult;

public interface GetUrlAnalyticsUseCase {

    UrlAnalyticsResult getAnalytics(String shortCode);
}