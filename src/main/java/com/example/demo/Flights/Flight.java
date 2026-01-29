package com.example.demo.Flights;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "flights")
@Getter
@Setter
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originAirportCode;
    private String originCountry;
    private String destinationAirportCode;
    private String destinationCountry;
    private String airline;
    private String flightNumber;
    private OffsetDateTime departureTime;
    private OffsetDateTime arrivalTime;
    private Integer adults;
    private String returnOriginAirportCode;
    private String returnDestinationAirportCode;
    private String returnAirline;
    private String returnFlightNumber;
    private OffsetDateTime returnDepartureTime;
    private OffsetDateTime returnArrivalTime;
    private BigDecimal price;
    private String currency;
    private String provider;
    private String offerId;
    @Column(columnDefinition = "LONGTEXT")
    private String rawOfferJson;
}
