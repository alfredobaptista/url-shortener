package com.github.alfredobaptista.application.exception;

public final class RateLimitExceededException
        extends RuntimeException {

    public RateLimitExceededException(String message) {
        super(message);
    }
}