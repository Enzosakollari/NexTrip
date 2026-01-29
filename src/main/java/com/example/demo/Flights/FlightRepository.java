package com.example.demo.Flights;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface FlightRepository extends JpaRepository<Flight, Long> {

    List<Flight> findByOriginAirportCodeAndDestinationAirportCode(
            String originAirportCode,
            String destinationAirportCode
    );

    List<Flight> findByOriginAirportCodeAndDestinationAirportCodeAndDepartureTimeBetween(
            String originAirportCode,
            String destinationAirportCode,
            OffsetDateTime from,
            OffsetDateTime to
    );

    Optional<Flight> findTopByOfferIdOrderByIdDesc(String offerId);
}
