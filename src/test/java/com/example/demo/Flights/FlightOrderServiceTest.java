package com.example.demo.Flights;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

@ExtendWith(MockitoExtension.class)
class FlightOrderServiceTest {

    @Mock
    private AmadeusClient amadeusClient;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private FlightOrderService flightOrderService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(flightOrderService, "baseUrl", "https://test.api.amadeus.com");
        ReflectionTestUtils.setField(flightOrderService, "restTemplate", restTemplate);
    }

    @Test
    void createOrder_whenValidOrder_returnsResult() {
        // Arrange
        FlightOrder order = new FlightOrder();
        order.setRawOfferJson("{\"id\":\"offer1\"}");
        order.setDateOfBirth(LocalDate.of(1990, 1, 1));
        order.setGender("Female");
        order.setFirstName("Jane");
        order.setLastName("Doe");
        order.setEmail("jane@example.com");
        order.setPhone("+1 (555) 123-4567");
        order.setPhoneCountryCode("+1");
        order.setNationality("US");
        order.setDocumentType("PASSPORT");
        order.setDocumentNumber("A1234567");
        order.setDocumentExpiry(LocalDate.of(2030, 1, 1));
        order.setDocumentIssuanceDate(LocalDate.of(2020, 1, 1));
        order.setDocumentIssuanceCountry("US");

        when(amadeusClient.getValidAccessToken()).thenReturn("token-123");

        String responseJson = "{\"data\":{\"id\":\"order123\",\"associatedRecords\":[{\"reference\":\"ABC123\"}]}}";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseJson, HttpStatus.OK);

        when(restTemplate.exchange(
                eq("https://test.api.amadeus.com/v1/booking/flight-orders"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);

        // Act
        FlightOrderService.FlightOrderResult result = flightOrderService.createOrder(order);

        // Assert
        assertNotNull(result);
        assertEquals("order123", result.orderId());
        assertEquals("ABC123", result.bookingReference());
        assertTrue(result.rawJson().contains("order123"));

        verify(amadeusClient).getValidAccessToken();
        verify(restTemplate).exchange(
                eq("https://test.api.amadeus.com/v1/booking/flight-orders"),
                eq(HttpMethod.POST),
                argThat(entity -> {
                    if (entity == null) {
                        return false;
                    }
                    String auth = entity.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                    Object bodyObj = entity.getBody();
                    String body = bodyObj == null ? null : bodyObj.toString();
                    return auth != null && auth.startsWith("Bearer ")
                            && body != null
                            && body.contains("\"flightOffers\"")
                            && body.contains("offer1");
                }),
                eq(String.class)
        );
    }

    @Test
    void createOrder_whenOfferMissing_throwsIllegalArgumentException() {
        // Arrange
        FlightOrder order = new FlightOrder();
        order.setRawOfferJson("  ");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                flightOrderService.createOrder(order)
        );
        assertEquals("Missing flight offer data for order.", exception.getMessage());

        verifyNoInteractions(amadeusClient, restTemplate);
    }
}
