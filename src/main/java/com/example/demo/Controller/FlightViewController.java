package com.example.demo.Controller;

import com.example.demo.Flights.Flight;
import com.example.demo.Flights.FlightInfoUtil;
import com.example.demo.Flights.FlightSearchHelper;
import com.example.demo.Flights.FlightService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class FlightViewController {

    private final FlightService flightService;
    private final FlightSearchHelper flightSearchHelper;

    @GetMapping("/flights/search-view")
    public String showSearchPage() {
        return "flights-search";
    }

    @GetMapping("/flights/search-view/results")
    public String searchFlights(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String returnDate,
            @RequestParam(required = false, defaultValue = "oneway") String tripType,
            @RequestParam(defaultValue = "1") int adults,
            @RequestParam(defaultValue = "EUR") String currency,
            Model model
    ) {
        // Set default date to tomorrow if not provided
        if (date == null || date.trim().isEmpty()) {
            date = java.time.LocalDate.now().plusDays(1).toString();
        }
        if (returnDate != null && !returnDate.isBlank()) {
            tripType = "roundtrip";
        }
        try {
            // Convert city/airport names to IATA codes if needed
            String originIata = flightSearchHelper.resolveIataCode(origin);
            String destinationIata = flightSearchHelper.resolveIataCode(destination);
            
            // Validate that we have valid 3-letter codes before proceeding
            if (originIata == null || originIata.length() != 3 || !originIata.matches("[A-Z]{3}")) {
                model.addAttribute("error", "Invalid origin: " + origin + ". Please enter a valid city name or 3-letter airport code.");
                model.addAttribute("origin", origin);
                model.addAttribute("destination", destination);
                model.addAttribute("date", date);
                model.addAttribute("returnDate", returnDate);
                model.addAttribute("tripType", tripType);
                model.addAttribute("adults", adults);
                model.addAttribute("currency", currency);
                return "flights-search";
            }
            
            if (destinationIata == null || destinationIata.length() != 3 || !destinationIata.matches("[A-Z]{3}")) {
                model.addAttribute("error", "Invalid destination: " + destination + ". Please enter a valid city name or 3-letter airport code.");
                model.addAttribute("origin", origin);
                model.addAttribute("destination", destination);
                model.addAttribute("date", date);
                model.addAttribute("returnDate", returnDate);
                model.addAttribute("tripType", tripType);
                model.addAttribute("adults", adults);
                model.addAttribute("currency", currency);
                return "flights-search";
            }
            
            if (returnDate != null && !returnDate.isBlank()) {
                try {
                    java.time.LocalDate depart = java.time.LocalDate.parse(date);
                    java.time.LocalDate ret = java.time.LocalDate.parse(returnDate);
                    if (ret.isBefore(depart)) {
                        model.addAttribute("error", "Return date must be the same day or after the departure date.");
                        model.addAttribute("origin", origin);
                        model.addAttribute("destination", destination);
                        model.addAttribute("date", date);
                        model.addAttribute("returnDate", returnDate);
                        model.addAttribute("tripType", tripType);
                        model.addAttribute("adults", adults);
                        model.addAttribute("currency", currency);
                        return "flights-search";
                    }
                } catch (java.time.format.DateTimeParseException ignored) {
                    // Let the API handle invalid dates; keep UI feedback consistent
                }
            }

            List<Flight> flights = flightService.searchOffers(originIata, destinationIata, date, returnDate, adults, currency);

            model.addAttribute("offers", flights);
            model.addAttribute("airlineNames", flights.stream()
                    .collect(java.util.stream.Collectors.toMap(
                            Flight::getOfferId,
                            f -> FlightInfoUtil.airlineName(f.getAirline())
                    )));
            model.addAttribute("origin", origin);
            model.addAttribute("destination", destination);
            model.addAttribute("originIata", originIata);
            model.addAttribute("destinationIata", destinationIata);
            model.addAttribute("date", date);
            model.addAttribute("returnDate", returnDate);
            model.addAttribute("tripType", tripType);
            model.addAttribute("adults", adults);
            model.addAttribute("currency", currency);

            return "flights-search";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("origin", origin);
            model.addAttribute("destination", destination);
            model.addAttribute("date", date);
            model.addAttribute("returnDate", returnDate);
            model.addAttribute("tripType", tripType);
            model.addAttribute("adults", adults);
            model.addAttribute("currency", currency);
            return "flights-search";
        } catch (Exception e) {
            model.addAttribute("error", "An error occurred while searching for flights: " + e.getMessage());
            model.addAttribute("origin", origin);
            model.addAttribute("destination", destination);
            model.addAttribute("date", date);
            model.addAttribute("returnDate", returnDate);
            model.addAttribute("tripType", tripType);
            model.addAttribute("adults", adults);
            model.addAttribute("currency", currency);
            return "flights-search";
        }
    }
    
}
