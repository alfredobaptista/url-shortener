package com.github.alfredobaptista.application.service;

import com.github.alfredobaptista.application.dto.CreateShortUrlCommand;
import com.github.alfredobaptista.application.dto.ShortUrlResult;
import com.github.alfredobaptista.application.exception.RateLimitExceededException;
import com.github.alfredobaptista.application.port.in.CreateShortUrlUseCase;
import com.github.alfredobaptista.application.port.out.AbuseChecker;
import com.github.alfredobaptista.application.port.out.ShortCodeGenerator;
import com.github.alfredobaptista.application.port.out.UrlCache;
import com.github.alfredobaptista.application.port.out.UrlRepository;
import com.github.alfredobaptista.domain.model.Url;
import com.github.alfredobaptista.domain.valueobject.OriginalUrl;
import com.github.alfredobaptista.domain.valueobject.ShortCode;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class CreateShortUrlService
        implements CreateShortUrlUseCase {

    private final UrlRepository urlRepository;
    private final UrlCache urlCache;
    private final ShortCodeGenerator shortCodeGenerator;
    private final AbuseChecker abuseChecker;

    public CreateShortUrlService(
            UrlRepository urlRepository,
            UrlCache urlCache,
            ShortCodeGenerator shortCodeGenerator,
            AbuseChecker abuseChecker
    ) {
        this.urlRepository = urlRepository;
        this.urlCache = urlCache;
        this.shortCodeGenerator = shortCodeGenerator;
        this.abuseChecker = abuseChecker;
    }

    @Override
    public ShortUrlResult createShortUrl(
            CreateShortUrlCommand command
    ) {
        String clientKey = command.clientKey();

        if (!abuseChecker.isAllowed(clientKey)) {
            throw new RateLimitExceededException(
                    "Limite de criação de URLs excedido. " +
                    "Tente novamente mais tarde."
            );
        }

        OriginalUrl originalUrl =
                new OriginalUrl(command.originalUrl());

        ShortCode shortCode =
                shortCodeGenerator.generate();

        Url url = new Url(
                shortCode,
                originalUrl,
                command.expiresAt()
        );

        urlRepository.save(url);

        Duration ttl = calculateCacheTtl(url);

        if (!ttl.isZero() && !ttl.isNegative()) {
            urlCache.save(
                    shortCode.getValue(),
                    originalUrl.getValue(),
                    ttl
            );
        }

        return new ShortUrlResult(
                shortCode.getValue()
        );
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