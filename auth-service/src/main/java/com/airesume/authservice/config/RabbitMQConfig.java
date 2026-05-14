package com.airesume.authservice.config;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for RabbitMQ Messaging Infrastructure.
 * Defines exchange names, routing keys, and JSON message converters for 
 * asynchronous inter-service communication (e.g., sending emails).
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "x.airesume";
    public static final String EMAIL_ROUTING_KEY = "email.send";

    /**
     * Configures a JSON converter to automatically serialize/deserialize 
     * message payloads (DTOs) sent over RabbitMQ.
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}
