package com.example.demo.WebSocket;

public record FlightSearchRequest(
        String requestId,
        String origin,
        String destination,
        String date,
        String returnDate,
        String tripType,
        Integer adults,
        String currency
) {
}
