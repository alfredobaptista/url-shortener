package com.github.alfredobaptista.adapter.in.web;

import com.github.alfredobaptista.application.port.in.RedirectUrlUseCase;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class RedirectController {

    private final RedirectUrlUseCase redirectUrlUseCase;

    public RedirectController(RedirectUrlUseCase redirectUrlUseCase) {
        this.redirectUrlUseCase = redirectUrlUseCase;
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(
            @PathVariable String shortCode
    ) {
        String originalUrl = redirectUrlUseCase.redirect(shortCode);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(originalUrl));

        return new ResponseEntity<>(
                headers,
                HttpStatus.FOUND
        );
    }
}