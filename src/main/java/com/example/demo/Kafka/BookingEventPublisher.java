package com.example.demo.Kafka;

public interface BookingEventPublisher {
    void publish(BookingEvent event);
}
