package com.github.alfredobaptista.application.service;

import com.github.alfredobaptista.application.port.in.RedirectUrlUseCase;
import com.github.alfredobaptista.application.port.out.AnalyticsPublisher;
import com.github.alfredobaptista.application.port.out.UrlCache;
import com.github.alfredobaptista.application.port.out.UrlRepository;
import com.github.alfredobaptista.domain.exception.UrlExpiredException;
import com.github.alfredobaptista.domain.exception.UrlNotFoundException;
import com.github.alfredobaptista.domain.model.Url;
import com.github.alfredobaptista.domain.valueobject.ShortCode;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class RedirectUrlService implements RedirectUrlUseCase {

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

        // 1. Valida o código recebido
        ShortCode code = new ShortCode(shortCode);

        // 2. Tenta obter a URL através do cache
        Optional<String> cachedUrl = urlCache.get(
                code.getValue()
        );

        if (cachedUrl.isPresent()) {

            publishAnalytics(code);

            return cachedUrl.get();
        }

        // 3. Cache miss → procura no banco de dados
        Url url = urlRepository.findByShortCode(code)
                .orElseThrow(() ->
                        new UrlNotFoundException(
                                "URL curta não encontrada: " + shortCode
                        )
                );

        // 4. Verifica se a URL expirou
        if (url.isExpired()) {
            throw new UrlExpiredException(
                    "Esta URL curta expirou."
            );
        }

        // 5. Calcula o TTL restante
        Duration ttl = calculateCacheTtl(url);

        // 6. Repopula o cache
        if (!ttl.isZero() && !ttl.isNegative()) {
            urlCache.save(
                    code.getValue(),
                    url.getOriginalUrl().getValue(),
                    ttl
            );
        }

        // 7. Publica o evento de analytics
        publishAnalytics(code);

        // 8. Retorna a URL original
        return url.getOriginalUrl().getValue();
    }

    private void publishAnalytics(ShortCode shortCode) {

        try {
            analyticsPublisher.publishRedirect(shortCode);
        } catch (Exception ignored) {
            // ignored.printStackTrace();
            // Falhas no sistema de analytics não devem
            // impedir o redireccionamento.
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