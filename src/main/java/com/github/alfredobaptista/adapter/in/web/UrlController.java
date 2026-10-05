package com.github.alfredobaptista.adapter.in.web;

import com.github.alfredobaptista.adapter.in.web.dto.CreateUrlRequest;
import com.github.alfredobaptista.adapter.in.web.dto.CreateUrlResponse;
import com.github.alfredobaptista.application.dto.CreateShortUrlCommand;
import com.github.alfredobaptista.application.dto.ShortUrlResult;
import com.github.alfredobaptista.application.dto.UrlAnalyticsResult;
import com.github.alfredobaptista.application.port.in.CreateShortUrlUseCase;
import com.github.alfredobaptista.application.port.in.DeleteShortUrlUseCase;
import com.github.alfredobaptista.application.port.in.GetUrlAnalyticsUseCase;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/urls")
public class UrlController {

    private final CreateShortUrlUseCase createShortUrlUseCase;
    private final DeleteShortUrlUseCase deleteShortUrlUseCase;
    private final GetUrlAnalyticsUseCase getUrlAnalyticsUseCase;

    public UrlController(
            CreateShortUrlUseCase createShortUrlUseCase,
            DeleteShortUrlUseCase deleteShortUrlUseCase,
            GetUrlAnalyticsUseCase getUrlAnalyticsUseCase
    ) {
        this.createShortUrlUseCase = createShortUrlUseCase;
        this.deleteShortUrlUseCase = deleteShortUrlUseCase;
        this.getUrlAnalyticsUseCase = getUrlAnalyticsUseCase;
    }


    @PostMapping
    public ResponseEntity<CreateUrlResponse> shorten(
                @Valid @RequestBody CreateUrlRequest request,
                HttpServletRequest httpRequest
) {
        String clientKey = httpRequest.getRemoteAddr();

        CreateShortUrlCommand command =
                new CreateShortUrlCommand(
                        request.originalUrl(),
                        request.expiresAt(),
                        clientKey
                );

        ShortUrlResult result =
                createShortUrlUseCase.createShortUrl(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new CreateUrlResponse(result.shortCode()));
    }

    @GetMapping("/{shortCode}/analytics")
    public ResponseEntity<UrlAnalyticsResult> analytics(
            @PathVariable String shortCode
    ) {
        UrlAnalyticsResult result =
                getUrlAnalyticsUseCase.getAnalytics(shortCode);

        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{shortCode}")
    public ResponseEntity<Void> delete(
            @PathVariable String shortCode
    ) {
        deleteShortUrlUseCase.deleteShortUrl(shortCode);

        return ResponseEntity
                .noContent()
                .build();
    }
}