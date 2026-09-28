package com.github.alfredobaptista.domain.exception;

public final class UrlExpiredException extends RuntimeException {

    public UrlExpiredException(String message) {
        super(message);
    }
}