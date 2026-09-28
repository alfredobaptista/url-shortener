package com.github.alfredobaptista.application.port.out;

public interface AbuseChecker {

    boolean isAllowed(String clientKey);
}