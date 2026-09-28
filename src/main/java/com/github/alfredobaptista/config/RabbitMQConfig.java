package com.github.alfredobaptista.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ANALYTICS_EXCHANGE =
            "url.analytics.exchange";

    public static final String ANALYTICS_QUEUE =
            "url.analytics.queue";

    public static final String ANALYTICS_ROUTING_KEY =
            "url.analytics.redirect";

    @Bean
    public DirectExchange analyticsExchange() {
        return new DirectExchange(ANALYTICS_EXCHANGE);
    }

    @Bean
    public Queue analyticsQueue() {
        return new Queue(
                ANALYTICS_QUEUE,
                true
        );
    }

    @Bean
    public Binding analyticsBinding(
            Queue analyticsQueue,
            DirectExchange analyticsExchange
    ) {
        return BindingBuilder
                .bind(analyticsQueue)
                .to(analyticsExchange)
                .with(ANALYTICS_ROUTING_KEY);
    }

    @Bean
    public JacksonJsonMessageConverter jacksonJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            JacksonJsonMessageConverter messageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory =
                new SimpleRabbitListenerContainerFactory();

        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);

        return factory;
    }
}