package com.example.demo.Kafka;

public record BookingEvent(
        String eventId,
        String eventType,
        String source,
        String status,
        Long bookingId,
        Long flightOrderId,
        String reference,
        String username,
        String occurredAt
) {
}
