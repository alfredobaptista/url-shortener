package com.github.alfredobaptista.application.port.out;

import java.time.Duration;
import java.util.Optional;

public interface UrlCache {

    Optional<String> get(String shortCode);

    void save(String shortCode, String originalUrl, Duration ttl);

    void delete(String shortCode);
}