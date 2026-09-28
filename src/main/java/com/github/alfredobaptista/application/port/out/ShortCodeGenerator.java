package com.github.alfredobaptista.application.port.out;

import com.github.alfredobaptista.domain.valueobject.ShortCode;

public interface ShortCodeGenerator {

    ShortCode generate();
}