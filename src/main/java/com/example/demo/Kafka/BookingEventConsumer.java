package com.example.demo.Kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true")
public class BookingEventConsumer {

    @KafkaListener(topics = "${kafka.topics.booking-events:booking.events}",
            groupId = "${spring.kafka.consumer.group-id:nextrip-booking}")
    public void handle(BookingEvent event) {
        if (event == null) {
            return;
        }
        log.info("Booking event received: type={}, source={}, bookingId={}, flightOrderId={}",
                event.eventType(), event.source(), event.bookingId(), event.flightOrderId());
    }
}
