package com.esmt.user.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange:smart-mobility.exchange}")
    private String exchange;

    @Bean
    public TopicExchange smartMobilityExchange() {
        // Exchange de type TOPIC pour un routage flexible
        return new TopicExchange(exchange);
    }

    @Bean
    public Queue userRegisteredQueue() {
        // Queue durable pour ne pas perdre de messages au redémarrage
        return QueueBuilder.durable("user.registered.queue").build();
    }

    @Bean
    public Binding userRegisteredBinding(Queue userRegisteredQueue, TopicExchange smartMobilityExchange) {
        // Liaison via la clé de routage spécifique
        return BindingBuilder.bind(userRegisteredQueue)
                .to(smartMobilityExchange)
                .with("user.registered.*");
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // pour gérer le champ LocalDateTime dans UserRegisteredEvent
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}