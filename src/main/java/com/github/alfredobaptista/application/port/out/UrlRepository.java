package com.github.alfredobaptista.application.port.out;

import com.github.alfredobaptista.domain.model.Url;
import com.github.alfredobaptista.domain.valueobject.ShortCode;

import java.util.Optional;

public interface UrlRepository {

    Url save(Url url);

    Optional<Url> findByShortCode(ShortCode shortCode);

    void deleteByShortCode(ShortCode shortCode);
}