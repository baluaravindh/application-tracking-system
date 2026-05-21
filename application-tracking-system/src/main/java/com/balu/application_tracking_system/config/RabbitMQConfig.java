package com.balu.application_tracking_system.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.queue.application}")
    private String applicationQueue;

    @Value("${rabbitmq.queue.interview}")
    private String interviewQueue;

    @Value("${rabbitmq.routing.key.application}")
    private String applicationRoutingKey;

    @Value("${rabbitmq.routing.key.interview}")
    private String interviewRoutingKey;

    // Declare application queue
    @Bean
    public Queue applicationQueue() {
        return new Queue(applicationQueue, true);
    }

    // Declare interview queue
    @Bean
    public Queue interviewQueue() {
        return new Queue(interviewQueue, true);
    }

    // Declare exchange
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchange);
    }

    // Bind application queue to exchange
    @Bean
    public Binding applicationBinding(Queue applicationQueue, TopicExchange exchange) {
        return BindingBuilder
                .bind(applicationQueue)
                .to(exchange)
                .with(applicationRoutingKey);
    }

    // Bind application queue to exchange
    @Bean
    public Binding interviewBinding(Queue interviewQueue, TopicExchange exchange) {
        return BindingBuilder
                .bind(interviewQueue)
                .to(exchange)
                .with(interviewRoutingKey);
    }

    // JSON message converter
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // RabbitMQ template with JSON converter
    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
