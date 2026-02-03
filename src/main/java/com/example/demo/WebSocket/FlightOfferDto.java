package com.example.demo.WebSocket;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record FlightOfferDto(
        String offerId,
        String originAirportCode,
        String destinationAirportCode,
        String airline,
        String airlineName,
        String flightNumber,
        String returnOriginAirportCode,
        String returnDestinationAirportCode,
        String returnAirline,
        String returnAirlineName,
        String returnFlightNumber,
        OffsetDateTime departureTime,
        OffsetDateTime arrivalTime,
        OffsetDateTime returnDepartureTime,
        OffsetDateTime returnArrivalTime,
        BigDecimal price,
        String currency
) {
}
