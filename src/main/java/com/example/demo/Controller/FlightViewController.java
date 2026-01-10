package com.example.demo.Controller;

import com.example.demo.Flights.Flight;
import com.example.demo.Flights.FlightService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class FlightViewController {

    private final FlightService flightService;

    @GetMapping("/flights/search-view")
    public String showSearchPage() {
        return "flights-search";
    }

    @GetMapping("/flights/search-view/results")
    public String searchFlights(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam(required = false) String date,
            @RequestParam(defaultValue = "1") int adults,
            @RequestParam(defaultValue = "EUR") String currency,
            Model model
    ) {
        // Set default date to tomorrow if not provided
        if (date == null || date.trim().isEmpty()) {
            date = java.time.LocalDate.now().plusDays(1).toString();
        }
        try {
            // Convert city/airport names to IATA codes if needed
            String originIata = convertToIataCode(origin);
            String destinationIata = convertToIataCode(destination);
            
            // Validate that we have valid 3-letter codes before proceeding
            if (originIata == null || originIata.length() != 3 || !originIata.matches("[A-Z]{3}")) {
                model.addAttribute("error", "Invalid origin: " + origin + ". Please enter a valid city name or 3-letter airport code.");
                model.addAttribute("origin", origin);
                model.addAttribute("destination", destination);
                model.addAttribute("date", date);
                model.addAttribute("adults", adults);
                model.addAttribute("currency", currency);
                return "flights-search";
            }
            
            if (destinationIata == null || destinationIata.length() != 3 || !destinationIata.matches("[A-Z]{3}")) {
                model.addAttribute("error", "Invalid destination: " + destination + ". Please enter a valid city name or 3-letter airport code.");
                model.addAttribute("origin", origin);
                model.addAttribute("destination", destination);
                model.addAttribute("date", date);
                model.addAttribute("adults", adults);
                model.addAttribute("currency", currency);
                return "flights-search";
            }
            
            List<Flight> flights = flightService.searchOffers(originIata, destinationIata, date, adults, currency);

            model.addAttribute("offers", flights);
            model.addAttribute("origin", origin);
            model.addAttribute("destination", destination);
            model.addAttribute("originIata", originIata);
            model.addAttribute("destinationIata", destinationIata);
            model.addAttribute("date", date);
            model.addAttribute("adults", adults);
            model.addAttribute("currency", currency);

            return "flights-search";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("origin", origin);
            model.addAttribute("destination", destination);
            model.addAttribute("date", date);
            model.addAttribute("adults", adults);
            model.addAttribute("currency", currency);
            return "flights-search";
        } catch (Exception e) {
            model.addAttribute("error", "An error occurred while searching for flights: " + e.getMessage());
            model.addAttribute("origin", origin);
            model.addAttribute("destination", destination);
            model.addAttribute("date", date);
            model.addAttribute("adults", adults);
            model.addAttribute("currency", currency);
            return "flights-search";
        }
    }
    
    /**
     * Convert city name or airport code to IATA code
     * If input is already a 3-letter code, return it
     * Otherwise, search for airports and return the first match
     */
    private String convertToIataCode(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Origin or destination cannot be empty");
        }
        
        String trimmed = input.trim();
        String upperTrimmed = trimmed.toUpperCase();
        
        // First, check if it's already a 3-letter IATA code
        if (upperTrimmed.length() == 3 && upperTrimmed.matches("[A-Z]{3}")) {
            return upperTrimmed;
        }
        
        // Try to extract IATA code from display name format: "City (IATA), Country" or "City (IATA)"
        // Match both uppercase and mixed case in parentheses
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\(([A-Z]{3})\\)", java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = pattern.matcher(trimmed);
        if (matcher.find()) {
            String code = matcher.group(1).toUpperCase();
            if (code.matches("[A-Z]{3}")) {
                return code;
            }
        }
        
        // Try to find 3-letter code anywhere in the string (for cases like "CDG Airport")
        pattern = java.util.regex.Pattern.compile("\\b([A-Z]{3})\\b");
        matcher = pattern.matcher(upperTrimmed);
        if (matcher.find()) {
            String found = matcher.group(1);
            // Make sure it's exactly 3 letters and valid
            if (found.length() == 3 && found.matches("[A-Z]{3}")) {
                return found;
            }
        }
        
        // Otherwise, search for airports using the original input
        try {
            List<Map<String, String>> airports = flightService.searchAirports(trimmed);
            if (!airports.isEmpty()) {
                String iataCode = airports.get(0).get("iataCode");
                if (iataCode != null && !iataCode.isEmpty() && iataCode.length() == 3 && iataCode.matches("[A-Z]{3}")) {
                    return iataCode.toUpperCase();
                }
            }
        } catch (Exception e) {
            // If airport search fails, log and continue with fallback logic
            System.err.println("Airport search failed for: " + trimmed + " - " + e.getMessage());
        }
        
        // Last resort: if input is exactly 3 characters and all letters, use it
        if (upperTrimmed.length() == 3 && upperTrimmed.matches("[A-Z]{3}")) {
            return upperTrimmed;
        }
        
        // If we can't convert it, throw an exception with a helpful message
        throw new IllegalArgumentException("Could not find a valid 3-letter airport code for: " + trimmed + 
                ". Please enter a city name or a valid 3-letter airport code (e.g., 'Paris' or 'CDG').");
    }
}
