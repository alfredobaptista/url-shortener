package com.github.alfredobaptista.adapter.in.messaging;

import com.github.alfredobaptista.adapter.out.messaging.AnalyticsEvent;
import com.github.alfredobaptista.application.service.AnalyticsService;
import com.github.alfredobaptista.config.RabbitMQConfig;
import com.github.alfredobaptista.domain.valueobject.ShortCode;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AnalyticsConsumer {

    private final AnalyticsService analyticsService;

    public AnalyticsConsumer(
            AnalyticsService analyticsService
    ) {
        this.analyticsService = analyticsService;
    }

    @RabbitListener(
            queues = RabbitMQConfig.ANALYTICS_QUEUE
    )
    public void consume(AnalyticsEvent event) {

        ShortCode shortCode =
                new ShortCode(event.shortCode());

        analyticsService.registerRedirect(shortCode);
    }
}