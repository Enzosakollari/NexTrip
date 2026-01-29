package com.example.demo.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.demo.Business.TravelPackage;
import com.example.demo.Flights.Flight;
import com.example.demo.Flights.FlightOrder;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.math.BigDecimal;
import java.math.RoundingMode;

class StripeServiceTest {

    @Test
    void createCheckoutSession_whenSecretKeyMissing_throwsIllegalStateException() {
        // Arrange
        StripeService stripeService = new StripeService(
                "",
                "eur",
                "http://success",
                "http://cancel",
                "http://flight-success",
                "http://flight-cancel"
        );
        TravelPackage pack = new TravelPackage();
        pack.setPrice(new BigDecimal("99.00"));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                stripeService.createCheckoutSession(pack, "John Doe", "john@example.com", "123", 2, "john")
        );
        assertEquals("Stripe is not configured. Set STRIPE_SECRET_KEY.", exception.getMessage());
    }

    @Test
    void createCheckoutSession_whenValidPack_returnsSessionAndBuildsParams() throws Exception {
        // Arrange
        StripeService stripeService = new StripeService(
                "sk_test_123",
                "eur",
                "http://success",
                "http://cancel",
                "http://flight-success",
                "http://flight-cancel"
        );
        TravelPackage pack = new TravelPackage();
        pack.setId(10L);
        pack.setTitle("Spring Adventure");
        pack.setPrice(new BigDecimal("199.99"));

        Session mockSession = mock(Session.class);

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            mockedSession.when(() -> Session.create(any(SessionCreateParams.class))).thenReturn(mockSession);

            // Act
            Session result = stripeService.createCheckoutSession(
                    pack,
                    "Jane Doe",
                    "jane@example.com",
                    "555-1111",
                    3,
                    "jane"
            );

            // Assert
            assertSame(mockSession, result);

            ArgumentCaptor<SessionCreateParams> captor = ArgumentCaptor.forClass(SessionCreateParams.class);
            mockedSession.verify(() -> Session.create(captor.capture()));

            SessionCreateParams params = captor.getValue();
            assertEquals(SessionCreateParams.Mode.PAYMENT, params.getMode());
            assertEquals("http://success", params.getSuccessUrl());
            assertEquals("http://cancel", params.getCancelUrl());

            assertNotNull(params.getLineItems());
            assertEquals(1, params.getLineItems().size());
            SessionCreateParams.LineItem lineItem = params.getLineItems().get(0);
            assertEquals(Long.valueOf(3L), lineItem.getQuantity());
            assertEquals("eur", lineItem.getPriceData().getCurrency());

            long expectedUnitAmount = new BigDecimal("199.99")
                    .multiply(new BigDecimal("100"))
                    .setScale(0, RoundingMode.HALF_UP)
                    .longValue();
            assertEquals(Long.valueOf(expectedUnitAmount), lineItem.getPriceData().getUnitAmount());
            assertEquals("Spring Adventure", lineItem.getPriceData().getProductData().getName());

            assertEquals("10", params.getMetadata().get("packId"));
            assertEquals("Jane Doe", params.getMetadata().get("fullName"));
            assertEquals("jane@example.com", params.getMetadata().get("email"));
            assertEquals("555-1111", params.getMetadata().get("phone"));
            assertEquals("3", params.getMetadata().get("travelersCount"));
            assertEquals("jane", params.getMetadata().get("username"));
        }
    }

    @Test
    void retrieveSession_whenSecretKeyMissing_throwsIllegalStateException() {
        // Arrange
        StripeService stripeService = new StripeService(
                " ",
                "eur",
                "http://success",
                "http://cancel",
                "http://flight-success",
                "http://flight-cancel"
        );

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                stripeService.retrieveSession("sess_123")
        );
        assertEquals("Stripe is not configured. Set STRIPE_SECRET_KEY.", exception.getMessage());
    }

    @Test
    void retrieveSession_whenValidId_returnsSession() throws Exception {
        // Arrange
        StripeService stripeService = new StripeService(
                "sk_test_123",
                "eur",
                "http://success",
                "http://cancel",
                "http://flight-success",
                "http://flight-cancel"
        );
        Session mockSession = mock(Session.class);

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            mockedSession.when(() -> Session.retrieve("sess_123")).thenReturn(mockSession);

            // Act
            Session result = stripeService.retrieveSession("sess_123");

            // Assert
            assertSame(mockSession, result);
            mockedSession.verify(() -> Session.retrieve("sess_123"));
        }
    }

    @Test
    void createFlightCheckoutSession_whenFlightPriceMissing_throwsIllegalArgumentException() {
        // Arrange
        StripeService stripeService = new StripeService(
                "sk_test_123",
                "eur",
                "http://success",
                "http://cancel",
                "http://flight-success",
                "http://flight-cancel"
        );
        Flight flight = new Flight();
        flight.setPrice(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                stripeService.createFlightCheckoutSession(flight, null)
        );
        assertEquals("Flight price is required", exception.getMessage());
    }

    @Test
    void createFlightCheckoutSession_whenValidFlight_returnsSessionAndBuildsParams() throws Exception {
        // Arrange
        StripeService stripeService = new StripeService(
                "sk_test_123",
                "eur",
                "http://success",
                "http://cancel",
                "http://flight-success",
                "http://flight-cancel"
        );
        Flight flight = new Flight();
        flight.setOriginAirportCode("JFK");
        flight.setDestinationAirportCode("LAX");
        flight.setPrice(new BigDecimal("350.50"));
        flight.setCurrency("USD");
        flight.setOfferId("offer-789");

        FlightOrder order = new FlightOrder();
        order.setId(42L);

        Session mockSession = mock(Session.class);

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            mockedSession.when(() -> Session.create(any(SessionCreateParams.class))).thenReturn(mockSession);

            // Act
            Session result = stripeService.createFlightCheckoutSession(flight, order);

            // Assert
            assertSame(mockSession, result);

            ArgumentCaptor<SessionCreateParams> captor = ArgumentCaptor.forClass(SessionCreateParams.class);
            mockedSession.verify(() -> Session.create(captor.capture()));

            SessionCreateParams params = captor.getValue();
            assertEquals(SessionCreateParams.Mode.PAYMENT, params.getMode());
            assertEquals("http://flight-success", params.getSuccessUrl());
            assertEquals("http://flight-cancel", params.getCancelUrl());

            assertNotNull(params.getLineItems());
            assertEquals(1, params.getLineItems().size());
            SessionCreateParams.LineItem lineItem = params.getLineItems().get(0);
            assertEquals(Long.valueOf(1L), lineItem.getQuantity());
            assertEquals("usd", lineItem.getPriceData().getCurrency());

            long expectedUnitAmount = new BigDecimal("350.50")
                    .multiply(new BigDecimal("100"))
                    .setScale(0, RoundingMode.HALF_UP)
                    .longValue();
            assertEquals(Long.valueOf(expectedUnitAmount), lineItem.getPriceData().getUnitAmount());

            String expectedTitle = "Flight JFK \u2192 LAX";
            assertEquals(expectedTitle, lineItem.getPriceData().getProductData().getName());

            assertEquals("42", params.getMetadata().get("flightOrderId"));
            assertEquals("offer-789", params.getMetadata().get("offerId"));
        }
    }
}