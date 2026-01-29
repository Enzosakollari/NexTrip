package com.example.demo.Flights;

import com.example.demo.User.AppUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "flight_orders")
@Getter
@Setter
public class FlightOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "app_user_id")
    private AppUser appUser;

    private String email;
    private String offerId;
    private String stripeSessionId;
    private String paymentStatus;
    private String orderStatus;
    private String amadeusOrderId;
    private String bookingReference;

    private BigDecimal price;
    private String currency;
    private String originAirportCode;
    private String destinationAirportCode;
    private OffsetDateTime departureTime;
    private OffsetDateTime arrivalTime;
    private String airline;
    private String flightNumber;
    private String returnOriginAirportCode;
    private String returnDestinationAirportCode;
    private OffsetDateTime returnDepartureTime;
    private OffsetDateTime returnArrivalTime;
    private String returnAirline;
    private String returnFlightNumber;

    private String firstName;
    private String lastName;
    private String gender;
    private LocalDate dateOfBirth;
    private String phone;
    private String phoneCountryCode;
    private String nationality;
    private String documentType;
    private String documentNumber;
    private LocalDate documentExpiry;
    private LocalDate documentIssuanceDate;
    private String documentIssuanceCountry;

    private LocalDateTime createdAt;

    @Column(columnDefinition = "LONGTEXT")
    private String rawOfferJson;

    @Column(columnDefinition = "LONGTEXT")
    private String rawOrderJson;

    @Column(length = 2000)
    private String errorMessage;
}
