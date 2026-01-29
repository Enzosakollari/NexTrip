package com.example.demo.Flights;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import com.example.demo.User.AppUser;

public interface FlightOrderRepository extends JpaRepository<FlightOrder, Long> {

    Optional<FlightOrder> findByStripeSessionId(String stripeSessionId);

    List<FlightOrder> findByEmailOrderByCreatedAtDesc(String email);

    List<FlightOrder> findByAppUserOrderByCreatedAtDesc(AppUser appUser);
}
