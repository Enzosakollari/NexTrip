package com.example.demo.Controller;

import com.example.demo.Flights.Flight;
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

    // Simple “landing-like” page for flights
    @GetMapping("/flights/search-view")
    public String showSearchPage() {
        return "flights-search";
    }

    // This handles the form submit and shows results on the same page
    @GetMapping("/flights/search-view/results")
    public String searchFlights(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam String date,          // YYYY-MM-DD
            @RequestParam(defaultValue = "1") int adults,
            @RequestParam(defaultValue = "EUR") String currency,
            Model model
    ) {
        List<Flight> flights = flightService.searchOffers(origin, destination, date, adults, currency);

        model.addAttribute("offers", flights);
        model.addAttribute("origin", origin);
        model.addAttribute("destination", destination);
        model.addAttribute("date", date);
        model.addAttribute("adults", adults);
        model.addAttribute("currency", currency);

        return "flights-search";
    }
}
