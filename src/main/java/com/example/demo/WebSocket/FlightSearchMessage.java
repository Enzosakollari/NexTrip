package com.example.demo.WebSocket;

public record FlightSearchMessage(
        String type,
        String requestId,
        String message,
        Object payload,
        Integer count
) {
}
