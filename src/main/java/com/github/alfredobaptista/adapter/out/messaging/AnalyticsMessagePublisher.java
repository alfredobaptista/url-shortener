package com.github.alfredobaptista.adapter.out.messaging;

import com.github.alfredobaptista.application.port.out.AnalyticsPublisher;
import com.github.alfredobaptista.config.RabbitMQConfig;
import com.github.alfredobaptista.domain.valueobject.ShortCode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class AnalyticsMessagePublisher implements AnalyticsPublisher {

    private final RabbitTemplate rabbitTemplate;

    public AnalyticsMessagePublisher(
            RabbitTemplate rabbitTemplate
    ) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publishRedirect(ShortCode shortCode) {

        AnalyticsEvent event =
                new AnalyticsEvent(shortCode.getValue());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ANALYTICS_EXCHANGE,
                RabbitMQConfig.ANALYTICS_ROUTING_KEY,
                event
        );
    }
}