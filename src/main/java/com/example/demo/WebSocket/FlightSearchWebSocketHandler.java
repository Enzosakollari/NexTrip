package com.example.demo.WebSocket;

import com.example.demo.Flights.Flight;
import com.example.demo.Flights.FlightInfoUtil;
import com.example.demo.Flights.FlightSearchHelper;
import com.example.demo.Flights.FlightService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FlightSearchWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final FlightService flightService;
    private final FlightSearchHelper flightSearchHelper;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        FlightSearchRequest request = objectMapper.readValue(message.getPayload(), FlightSearchRequest.class);
        String requestIdValue = request.requestId();
        if (requestIdValue == null || requestIdValue.isBlank()) {
            requestIdValue = UUID.randomUUID().toString();
        }
        final String requestId = requestIdValue;

        send(session, new FlightSearchMessage("status", requestId, "started", null, null));

        try {
            String originIata = flightSearchHelper.resolveIataCode(request.origin());
            String destinationIata = flightSearchHelper.resolveIataCode(request.destination());

            String departureDateIso = normalizeDate(request.date());
            String tripType = request.tripType() == null ? "oneway" : request.tripType().trim().toLowerCase(Locale.ROOT);
            String returnDateIso = normalizeReturnDate(tripType, request.returnDate());

            if (returnDateIso != null && !returnDateIso.isBlank()) {
                try {
                    LocalDate depart = LocalDate.parse(departureDateIso);
                    LocalDate ret = LocalDate.parse(returnDateIso);
                    if (ret.isBefore(depart)) {
                        throw new IllegalArgumentException("Return date must be the same day or after the departure date.");
                    }
                } catch (DateTimeParseException ignored) {
                    // Allow API to handle invalid dates; keep UI feedback consistent.
                }
            }

            int adults = request.adults() != null && request.adults() > 0 ? request.adults() : 1;
            String currency = request.currency() == null || request.currency().isBlank()
                    ? "EUR"
                    : request.currency().trim().toUpperCase(Locale.ROOT);

            int[] count = new int[]{0};

            flightService.searchOffersStreaming(
                    originIata,
                    destinationIata,
                    departureDateIso,
                    returnDateIso,
                    adults,
                    currency,
                    flight -> {
                        FlightOfferDto dto = toDto(flight);
                        try {
                            send(session, new FlightSearchMessage("offer", requestId, null, dto, null));
                            count[0]++;
                        } catch (IOException e) {
                            System.err.println("Failed to send flight offer: " + e.getMessage());
                        }
                    }
            );

            send(session, new FlightSearchMessage("complete", requestId, "complete", null, count[0]));
        } catch (Exception e) {
            send(session, new FlightSearchMessage("error", requestId, e.getMessage(), null, null));
        }
    }

    private String normalizeDate(String dateValue) {
        if (dateValue == null || dateValue.isBlank()) {
            return LocalDate.now().plusDays(1).toString();
        }
        return dateValue.trim();
    }

    private String normalizeReturnDate(String tripType, String returnDateValue) {
        if ("roundtrip".equals(tripType)) {
            if (returnDateValue == null || returnDateValue.isBlank()) {
                throw new IllegalArgumentException("Return date is required for round-trip searches.");
            }
            return returnDateValue.trim();
        }
        if (returnDateValue == null || returnDateValue.isBlank()) {
            return null;
        }
        return returnDateValue.trim();
    }

    private FlightOfferDto toDto(Flight flight) {
        return new FlightOfferDto(
                flight.getOfferId(),
                flight.getOriginAirportCode(),
                flight.getDestinationAirportCode(),
                flight.getAirline(),
                FlightInfoUtil.airlineName(flight.getAirline()),
                flight.getFlightNumber(),
                flight.getReturnOriginAirportCode(),
                flight.getReturnDestinationAirportCode(),
                flight.getReturnAirline(),
                FlightInfoUtil.airlineName(flight.getReturnAirline()),
                flight.getReturnFlightNumber(),
                flight.getDepartureTime(),
                flight.getArrivalTime(),
                flight.getReturnDepartureTime(),
                flight.getReturnArrivalTime(),
                flight.getPrice(),
                flight.getCurrency()
        );
    }

    private void send(WebSocketSession session, FlightSearchMessage message) throws IOException {
        if (session != null && session.isOpen()) {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
        }
    }
}
