package com.github.alfredobaptista.application.port.in;

import com.github.alfredobaptista.application.dto.CreateShortUrlCommand;
import com.github.alfredobaptista.application.dto.ShortUrlResult;

public interface CreateShortUrlUseCase {

    ShortUrlResult createShortUrl(CreateShortUrlCommand command);
}