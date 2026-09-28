package com.github.alfredobaptista.adapter.out.generator;

import com.github.alfredobaptista.application.port.out.ShortCodeGenerator;
import com.github.alfredobaptista.domain.valueobject.ShortCode;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class Base62ShortCodeGeneratorAdapter
        implements ShortCodeGenerator {

    private static final String ALPHABET =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static final int CODE_LENGTH = 6;

    private final SecureRandom random = new SecureRandom();

    @Override
    public ShortCode generate() {

        StringBuilder code =
                new StringBuilder(CODE_LENGTH);

        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(
                    ALPHABET.charAt(
                            random.nextInt(ALPHABET.length())
                    )
            );
        }

        return new ShortCode(code.toString());
    }
}