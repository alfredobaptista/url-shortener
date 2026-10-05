package com.github.alfredobaptista.application.service;

import com.github.alfredobaptista.application.port.in.RedirectUrlUseCase;
import com.github.alfredobaptista.application.port.out.AnalyticsPublisher;
import com.github.alfredobaptista.application.port.out.UrlCache;
import com.github.alfredobaptista.application.port.out.UrlRepository;
import com.github.alfredobaptista.domain.exception.UrlExpiredException;
import com.github.alfredobaptista.domain.exception.UrlNotFoundException;
import com.github.alfredobaptista.domain.model.Url;
import com.github.alfredobaptista.domain.valueobject.ShortCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class RedirectUrlService implements RedirectUrlUseCase {

    private static final Logger log =
            LoggerFactory.getLogger(RedirectUrlService.class);

    private final UrlRepository urlRepository;
    private final UrlCache urlCache;
    private final AnalyticsPublisher analyticsPublisher;

    public RedirectUrlService(
            UrlRepository urlRepository,
            UrlCache urlCache,
            AnalyticsPublisher analyticsPublisher
    ) {
        this.urlRepository = urlRepository;
        this.urlCache = urlCache;
        this.analyticsPublisher = analyticsPublisher;
    }

    @Override
    public String redirect(String shortCode) {

        ShortCode code = new ShortCode(shortCode);

        
        Optional<String> cachedUrl = urlCache.get(
                code.getValue()
        );

        if (cachedUrl.isPresent()) {

            publishAnalytics(code);

            return cachedUrl.get();
        }

        Url url = urlRepository.findByShortCode(code)
                .orElseThrow(() ->
                        new UrlNotFoundException(
                                "URL curta não encontrada: " + shortCode
                        )
                );

      
        if (url.isExpired()) {
            throw new UrlExpiredException(
                    "Esta URL curta expirou."
            );
        }

        Duration ttl = calculateCacheTtl(url);

        if (!ttl.isZero() && !ttl.isNegative()) {
            urlCache.save(
                    code.getValue(),
                    url.getOriginalUrl().getValue(),
                    ttl
            );
        }

        publishAnalytics(code);

        return url.getOriginalUrl().getValue();
    }

    private void publishAnalytics(ShortCode shortCode) {

        try {
            analyticsPublisher.publishRedirect(shortCode);
        } catch (Exception e) {

      
            log.warn(
                    "Não foi possível publicar o evento de analytics " +
                    "para shortCode={}",
                    shortCode.getValue(),
                    e
            );
        }
    }

    private Duration calculateCacheTtl(Url url) {

        if (url.getExpiresAt() == null) {
            return Duration.ofHours(24);
        }

        return Duration.between(
                LocalDateTime.now(),
                url.getExpiresAt()
        );
    }
}