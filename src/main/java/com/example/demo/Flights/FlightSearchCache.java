package com.example.demo.Flights;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class FlightSearchCache {

    private final RedisTemplate<String, Flight> flightRedisTemplate;

    @Value("${flights.cache.ttl-minutes:30}")
    private long ttlMinutes;

    public void cacheOffer(Flight flight) {
        if (flight == null || flight.getOfferId() == null || flight.getOfferId().isBlank()) {
            return;
        }
        try {
            flightRedisTemplate.opsForValue().set(
                    keyForOffer(flight.getOfferId()),
                    flight,
                    Duration.ofMinutes(ttlMinutes)
            );
        } catch (Exception e) {
            System.err.println("Failed to cache flight offer: " + e.getMessage());
        }
    }

    public Flight getOffer(String offerId) {
        if (offerId == null || offerId.isBlank()) {
            return null;
        }
        try {
            return flightRedisTemplate.opsForValue().get(keyForOffer(offerId));
        } catch (Exception e) {
            System.err.println("Failed to read cached flight offer: " + e.getMessage());
            return null;
        }
    }

    private String keyForOffer(String offerId) {
        return "flight:offer:" + offerId.trim();
    }
}
