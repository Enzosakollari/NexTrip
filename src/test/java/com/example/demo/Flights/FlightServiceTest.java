package com.example.demo.Flights;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class FlightServiceTest {

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private AmadeusClient amadeusClient;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private FlightService flightService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(flightService, "baseUrl", "https://test.api.amadeus.com");
        ReflectionTestUtils.setField(flightService, "restTemplate", restTemplate);
    }

    @Test
    void searchAirports_returnsAirports_whenApiCallSucceeds() {
        // Arrange
        String keyword = "New York";
        String accessToken = "test-token";

        when(amadeusClient.getValidAccessToken()).thenReturn(accessToken);

        // Create mock response
        ObjectNode airport = objectMapper.createObjectNode();
        airport.put("iataCode", "JFK");
        airport.put("name", "John F Kennedy International Airport");

        ObjectNode address = objectMapper.createObjectNode();
        address.put("cityName", "New York");
        address.put("countryName", "United States");
        airport.set("address", address);

        ArrayNode data = objectMapper.createArrayNode();
        data.add(airport);

        ObjectNode root = objectMapper.createObjectNode();
        root.set("data", data);

        ResponseEntity<String> responseEntity = new ResponseEntity<>(root.toString(), HttpStatus.OK);

        when(restTemplate.exchange(
                contains("/v1/reference-data/locations"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);

        // Act
        List<Map<String, String>> result = flightService.searchAirports(keyword);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("JFK", result.get(0).get("iataCode"));
        assertEquals("New York", result.get(0).get("cityName"));
        assertEquals("United States", result.get(0).get("countryName"));
        assertEquals("New York (JFK), United States", result.get(0).get("displayName"));

        verify(amadeusClient).getValidAccessToken();
        verify(restTemplate).exchange(
                contains("/v1/reference-data/locations"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        );
    }

    @Test
    void searchAirports_returnsEmptyList_whenApiCallFails() {
        // Arrange
        String keyword = "Invalid";
        String accessToken = "test-token";

        when(amadeusClient.getValidAccessToken()).thenReturn(accessToken);

        ResponseEntity<String> responseEntity = new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        when(restTemplate.exchange(
                contains("/v1/reference-data/locations"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);

        // Act
        List<Map<String, String>> result = flightService.searchAirports(keyword);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(amadeusClient).getValidAccessToken();
        verify(restTemplate).exchange(
                contains("/v1/reference-data/locations"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        );
    }

    @Test
    void searchOffers_returnsFlights_whenApiCallSucceeds() {
        // Arrange
        String originIata = "JFK";
        String destinationIata = "LAX";
        String departureDateIso = "2023-12-25";
        int adults = 1;
        String currency = "USD";
        String accessToken = "test-token";

        when(amadeusClient.getValidAccessToken()).thenReturn(accessToken);

        // Create mock response
        ObjectNode offer = objectMapper.createObjectNode();
        offer.put("id", "offer1");

        ObjectNode price = objectMapper.createObjectNode();
        price.put("total", "199.99");
        price.put("currency", "USD");
        offer.set("price", price);

        ObjectNode departure = objectMapper.createObjectNode();
        departure.put("iataCode", "JFK");
        departure.put("at", "2023-12-25T08:00:00Z");

        ObjectNode arrival = objectMapper.createObjectNode();
        arrival.put("iataCode", "LAX");
        arrival.put("at", "2023-12-25T11:00:00Z");

        ObjectNode segment = objectMapper.createObjectNode();
        segment.put("carrierCode", "AA");
        segment.put("number", "123");
        segment.set("departure", departure);
        segment.set("arrival", arrival);

        ArrayNode segments = objectMapper.createArrayNode();
        segments.add(segment);

        ObjectNode itinerary = objectMapper.createObjectNode();
        itinerary.set("segments", segments);

        ArrayNode itineraries = objectMapper.createArrayNode();
        itineraries.add(itinerary);

        offer.set("itineraries", itineraries);

        ArrayNode data = objectMapper.createArrayNode();
        data.add(offer);

        ObjectNode root = objectMapper.createObjectNode();
        root.set("data", data);

        ResponseEntity<String> responseEntity = new ResponseEntity<>(root.toString(), HttpStatus.OK);

        when(restTemplate.exchange(
                contains("/v2/shopping/flight-offers"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);

        // Act
        List<Flight> result = flightService.searchOffers(originIata, destinationIata, departureDateIso, adults, currency);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("offer1", result.get(0).getOfferId());
        assertEquals(new BigDecimal("199.99"), result.get(0).getPrice());
        assertEquals("USD", result.get(0).getCurrency());
        assertEquals("AA", result.get(0).getAirline());
        assertEquals("123", result.get(0).getFlightNumber());
        assertEquals("JFK", result.get(0).getOriginAirportCode());
        assertEquals("LAX", result.get(0).getDestinationAirportCode());
        assertNotNull(result.get(0).getDepartureTime());
        assertNotNull(result.get(0).getArrivalTime());

        verify(amadeusClient).getValidAccessToken();
        verify(restTemplate).exchange(
                contains("/v2/shopping/flight-offers"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        );
        verify(flightRepository).saveAll(anyList());
    }

    @Test
    void refreshPopularRoutes_callsSearchOffers_forEachRoute() {
        // Create a spy of the service
        FlightService spyService = spy(flightService);

        // Arrange - stub the searchOffers method to return empty list
        doReturn(new ArrayList<Flight>()).when(spyService).searchOffers(anyString(), anyString(), anyString(), anyInt(), anyString());

        // Act
        spyService.refreshPopularRoutes();

        // Assert - verify searchOffers was called 3 times (for each route)
        verify(spyService, times(3)).searchOffers(anyString(), anyString(), anyString(), anyInt(), anyString());
    }
}
