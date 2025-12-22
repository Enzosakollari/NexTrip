package com.example.demo.Flights;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FlightService {

    private final FlightRepository flightRepository;
    private final AmadeusClient amadeusClient;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${amadeus.api.base-url}")
    private String baseUrl;

    // Main method your controller calls
    public List<Flight> searchOffers(
            String originIata,
            String destinationIata,
            String departureDateIso, // "YYYY-MM-DD"
            int adults,
            String currency
    ) {
        try {
            String url = baseUrl + "/v2/shopping/flight-offers"
                    + "?originLocationCode=" + originIata
                    + "&destinationLocationCode=" + destinationIata
                    + "&departureDate=" + departureDateIso
                    + "&adults=" + adults
                    + "&currencyCode=" + currency
                    + "&max=10";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(amadeusClient.getValidAccessToken());
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Amadeus search failed: " + response.getStatusCode());
            }

            JsonNode root = mapper.readTree(response.getBody());
            JsonNode data = root.path("data");

            List<Flight> flights = new ArrayList<>();

            if (data != null && data.isArray()) {
                for (JsonNode item : data) {
                    Flight f = mapOffer(item);
                    // override with query params just to be safe
                    f.setOriginAirportCode(originIata);
                    f.setDestinationAirportCode(destinationIata);
                    f.setProvider("AMADEUS");
                    flights.add(f);
                }
            }

            flightRepository.saveAll(flights);
            return flights;

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch flight offers from Amadeus", e);
        }
    }

    private Flight mapOffer(JsonNode item) {
        Flight f = new Flight();

        f.setOfferId(item.path("id").asText(""));

        // price
        JsonNode priceNode = item.path("price");
        BigDecimal total = new BigDecimal(priceNode.path("total").asText("0"));
        String currency = priceNode.path("currency").asText("EUR");
        f.setPrice(total);
        f.setCurrency(currency);

        // first itinerary / first segment
        JsonNode itineraries = item.path("itineraries");
        if (itineraries.isArray() && itineraries.size() > 0) {
            JsonNode firstItinerary = itineraries.get(0);
            JsonNode segments = firstItinerary.path("segments");
            if (segments.isArray() && segments.size() > 0) {
                JsonNode firstSegment = segments.get(0);

                String carrierCode = firstSegment.path("carrierCode").asText("");
                String flightNumber = firstSegment.path("number").asText("");

                f.setAirline(carrierCode);
                f.setFlightNumber(flightNumber);

                JsonNode departure = firstSegment.path("departure");
                JsonNode arrival = firstSegment.path("arrival");

                String depAirport = departure.path("iataCode").asText("");
                String arrAirport = arrival.path("iataCode").asText("");

                f.setOriginAirportCode(depAirport);
                f.setDestinationAirportCode(arrAirport);

                String depTime = departure.path("at").asText(null);
                String arrTime = arrival.path("at").asText(null);

                if (depTime != null && !depTime.isEmpty()) {
                    f.setDepartureTime(parseAmadeusDateTime(depTime));
                }
                if (arrTime != null && !arrTime.isEmpty()) {
                    f.setArrivalTime(parseAmadeusDateTime(arrTime));
                }
            }
        }

        // countries can be filled later via a Locations API if you want
        f.setOriginCountry(null);
        f.setDestinationCountry(null);

        return f;
    }

    // Handle both "2025-12-20T04:00:00Z" and "2025-12-20T04:00:00"
    private OffsetDateTime parseAmadeusDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            // Has timezone info (Z or +01:00 etc.)
            if (value.length() > 19) {
                return OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            } else {
                // No offset, assume local date-time, convert to UTC
                LocalDateTime ldt =
                        LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                return ldt.atOffset(ZoneOffset.UTC);
            }
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Cannot parse datetime from Amadeus: " + value, e);
        }
    }

    // Optional: pre-fetch some routes every 2 hours (you can delete this if you don't want it yet)
    @Scheduled(cron = "0 0 */2 * * *")
    public void refreshPopularRoutes() {
        List<String[]> routes = List.of(
                new String[]{"TIA", "BUD"},
                new String[]{"TIA", "FCO"},
                new String[]{"TIA", "FRA"}
        );

        String currency = "EUR";
        int adults = 1;

        java.time.LocalDate target = java.time.LocalDate.now().plusWeeks(1);
        String departureDateIso = target.toString(); // "YYYY-MM-DD"

        for (String[] r : routes) {
            searchOffers(r[0], r[1], departureDateIso, adults, currency);
        }
    }
}
