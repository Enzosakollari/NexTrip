package com.example.demo.Kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "false", matchIfMissing = true)
public class NoopBookingEventPublisher implements BookingEventPublisher {
    @Override
    public void publish(BookingEvent event) {
        if (event != null) {
            log.debug("Kafka disabled, skipping event {}", event.eventType());
        }
    }
}
