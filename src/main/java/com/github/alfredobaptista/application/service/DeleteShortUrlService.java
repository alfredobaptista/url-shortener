package com.github.alfredobaptista.application.service;

import com.github.alfredobaptista.application.port.in.DeleteShortUrlUseCase;
import com.github.alfredobaptista.application.port.out.UrlCache;
import com.github.alfredobaptista.application.port.out.UrlRepository;
import com.github.alfredobaptista.domain.exception.UrlNotFoundException;
import com.github.alfredobaptista.domain.valueobject.ShortCode;
import org.springframework.stereotype.Service;

@Service
public class DeleteShortUrlService implements DeleteShortUrlUseCase {

    private final UrlRepository urlRepository;
    private final UrlCache urlCache;

    public DeleteShortUrlService(
            UrlRepository urlRepository,
            UrlCache urlCache
    ) {
        this.urlRepository = urlRepository;
        this.urlCache = urlCache;
    }

    @Override
    public void deleteShortUrl(String shortCode) {

        ShortCode code = new ShortCode(shortCode);

        urlRepository.findByShortCode(code)
                .orElseThrow(() ->
                        new UrlNotFoundException(
                                "URL curta não encontrada: " + shortCode
                        )
                );

        urlRepository.deleteByShortCode(code);

        urlCache.delete(code.getValue());
    }
}