package com.example.demo.Booking;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingStatusBackfill implements CommandLineRunner {

    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public void run(String... args) {
        bookingRepository.normalizeStatuses();
    }
}
