package com.example.demo.Flights;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "flights")   // this will now store ticket offers
@Getter
@Setter
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Origin
    private String originAirportCode;        // e.g. "TIA"
    private String originCountry;            // optional for now

    // Destination
    private String destinationAirportCode;   // e.g. "BUD"
    private String destinationCountry;       // optional for now

    // Airline & flight number
    private String airline;                  // e.g. "W6"
    private String flightNumber;             // e.g. "6621"

    // Times
    private OffsetDateTime departureTime;
    private OffsetDateTime arrivalTime;

    // Price
    private BigDecimal price;
    private String currency;                 // e.g. "EUR"

    // Meta
    private String provider;                 // e.g. "AMADEUS"
    private String offerId;                  // Amadeus data[i].id
}
