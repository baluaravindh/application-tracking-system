package com.balu.application_tracking_system.messaging;

import com.balu.application_tracking_system.dto.ApplicationNotificationDTO;
import com.balu.application_tracking_system.dto.InterviewNotificationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPublisher {

    @Value("${rabbitmq.routing.key.application}")
    private String applicationRoutingKey;

    @Value("${rabbitmq.routing.key.interview}")
    private String interviewRoutingKey;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    private final AmqpTemplate amqpTemplate;

    // Publish application notification
    public void publishApplicationNotification(ApplicationNotificationDTO message) {
        amqpTemplate.convertAndSend(exchange, applicationRoutingKey, message);
        log.info("Application notification published: {}", message);
    }

    // Publish interview notification
    public void publishInterviewNotification(InterviewNotificationDTO message) {
        amqpTemplate.convertAndSend(exchange, interviewRoutingKey, message);
        log.info("Interview notification published: {}", message);
    }
}
