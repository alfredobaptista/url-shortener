package com.github.alfredobaptista.domain.exception;

public final class RateLimitExceededException
        extends RuntimeException {

    public RateLimitExceededException(String message) {
        super(message);
    }
}