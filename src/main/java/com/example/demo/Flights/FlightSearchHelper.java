package com.example.demo.Flights;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class FlightSearchHelper {

    private final FlightService flightService;

    public String resolveIataCode(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Origin or destination cannot be empty");
        }

        String trimmed = input.trim();
        String upperTrimmed = trimmed.toUpperCase(Locale.ROOT);

        if (upperTrimmed.length() == 3 && upperTrimmed.matches("[A-Z]{3}")) {
            return upperTrimmed;
        }

        Pattern pattern = Pattern.compile("\\(([A-Z]{3})\\)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(trimmed);
        if (matcher.find()) {
            String code = matcher.group(1).toUpperCase(Locale.ROOT);
            if (code.matches("[A-Z]{3}")) {
                return code;
            }
        }

        pattern = Pattern.compile("\\b([A-Z]{3})\\b");
        matcher = pattern.matcher(upperTrimmed);
        if (matcher.find()) {
            String found = matcher.group(1);
            if (found.length() == 3 && found.matches("[A-Z]{3}")) {
                return found;
            }
        }

        try {
            List<Map<String, String>> airports = flightService.searchAirports(trimmed);
            if (!airports.isEmpty()) {
                String iataCode = airports.get(0).get("iataCode");
                if (iataCode != null && !iataCode.isEmpty()
                        && iataCode.length() == 3
                        && iataCode.matches("[A-Z]{3}")) {
                    return iataCode.toUpperCase(Locale.ROOT);
                }
            }
        } catch (Exception e) {
            System.err.println("Airport search failed for: " + trimmed + " - " + e.getMessage());
        }

        if (upperTrimmed.length() == 3 && upperTrimmed.matches("[A-Z]{3}")) {
            return upperTrimmed;
        }

        throw new IllegalArgumentException("Could not find a valid 3-letter airport code for: " + trimmed
                + ". Please enter a city name or a valid 3-letter airport code (e.g., 'Paris' or 'CDG').");
    }
}
