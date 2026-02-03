package com.example.demo.Kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true")
public class KafkaBookingEventPublisher implements BookingEventPublisher {

    private final KafkaTemplate<String, BookingEvent> kafkaTemplate;

    @Value("${kafka.topics.booking-events:booking.events}")
    private String topic;

    @Override
    public void publish(BookingEvent event) {
        if (event == null) {
            return;
        }
        try {
            kafkaTemplate.send(topic, event.eventId(), event);
        } catch (Exception e) {
            log.warn("Failed to publish booking event: {}", e.getMessage());
        }
    }
}
