package com.github.alfredobaptista.application.port.in;

import com.github.alfredobaptista.domain.valueobject.ShortCode;

public interface RegisterRedirectAnalyticsUseCase {

    void registerRedirect(ShortCode shortCode);
}