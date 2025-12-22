package com.example.demo.Controller;

import com.example.demo.Flights.Flight;
import com.example.demo.Flights.FlightRepository;
import com.example.demo.Flights.FlightService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;
    private final FlightRepository flightRepository;

    // 1) Search flights via Amadeus
    @GetMapping("/search")
    public List<Flight> search(
            @RequestParam String origin,          // IATA, e.g. "TIA"
            @RequestParam String destination,     // IATA, e.g. "BUD"
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "1") int adults,
            @RequestParam(defaultValue = "EUR") String currency
    ) {
        return flightService.searchOffers(
                origin,
                destination,
                date.toString(),   // "YYYY-MM-DD"
                adults,
                currency
        );
    }

    // 2) Get cached offers from DB (if you don’t want to hit Amadeus every time)
    @GetMapping("/cached")
    public List<Flight> cached(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        if (date == null) {
            return flightRepository
                    .findByOriginAirportCodeAndDestinationAirportCode(origin, destination);
        } else {
            OffsetDateTime start = date.atStartOfDay().atOffset(java.time.ZoneOffset.UTC);
            OffsetDateTime end = start.plusDays(1);
            return flightRepository
                    .findByOriginAirportCodeAndDestinationAirportCodeAndDepartureTimeBetween(
                            origin, destination, start, end);
        }
    }
}
