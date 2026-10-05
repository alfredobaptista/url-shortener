package com.github.alfredobaptista.adapter.in.messaging;

import com.github.alfredobaptista.adapter.out.messaging.AnalyticsEvent;
import com.github.alfredobaptista.application.port.in.RegisterRedirectAnalyticsUseCase;
import com.github.alfredobaptista.config.RabbitMQConfig;
import com.github.alfredobaptista.domain.valueobject.ShortCode;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AnalyticsConsumer {

    private final RegisterRedirectAnalyticsUseCase registerRedirectAnalyticsUseCase;

    public AnalyticsConsumer(
            RegisterRedirectAnalyticsUseCase registerRedirectAnalyticsUseCase
    ) {
        this.registerRedirectAnalyticsUseCase =
                registerRedirectAnalyticsUseCase;
    }

    @RabbitListener(
            queues = RabbitMQConfig.ANALYTICS_QUEUE
    )
    public void consume(AnalyticsEvent event) {

        ShortCode shortCode =
                new ShortCode(event.shortCode());

        registerRedirectAnalyticsUseCase.registerRedirect(
                shortCode
        );
    }
}