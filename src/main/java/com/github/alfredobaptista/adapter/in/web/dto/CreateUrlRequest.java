package com.github.alfredobaptista.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record CreateUrlRequest(

        @NotBlank(message = "A URL original é obrigatória.")
        String originalUrl,

        LocalDateTime expiresAt

) {
}