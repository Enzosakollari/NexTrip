package com.example.demo.Booking;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingStatusSchemaFix implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            jdbcTemplate.execute("ALTER TABLE bookings MODIFY COLUMN status VARCHAR(20)");
        } catch (Exception ex) {
            System.err.println("Booking status schema fix skipped: " + ex.getMessage());
        }
    }
}
