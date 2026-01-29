package com.example.demo.Service;

import com.example.demo.Business.TravelPackage;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Service
public class StripeService {

    private final String secretKey;
    private final String currency;
    private final String successUrl;
    private final String cancelUrl;
    private final String flightSuccessUrl;
    private final String flightCancelUrl;

    public StripeService(
            @Value("${stripe.secret-key}") String secretKey,
            @Value("${stripe.currency:eur}") String currency,
            @Value("${stripe.success-url}") String successUrl,
            @Value("${stripe.cancel-url}") String cancelUrl,
            @Value("${stripe.flight-success-url:http://localhost:8080/flights/checkout-success?session_id={CHECKOUT_SESSION_ID}}")
            String flightSuccessUrl,
            @Value("${stripe.flight-cancel-url:http://localhost:8080/flights/checkout-cancel}") String flightCancelUrl
    ) {
        this.secretKey = secretKey;
        this.currency = currency;
        this.successUrl = successUrl;
        this.cancelUrl = cancelUrl;
        this.flightSuccessUrl = flightSuccessUrl;
        this.flightCancelUrl = flightCancelUrl;
    }

    public Session createCheckoutSession(
            TravelPackage pack,
            String fullName,
            String email,
            String phone,
            int travelersCount,
            String username
    ) {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("Stripe is not configured. Set STRIPE_SECRET_KEY.");
        }
        if (pack.getPrice() == null) {
            throw new IllegalArgumentException("Pack price is required");
        }

        Stripe.apiKey = secretKey;
        long unitAmount = pack.getPrice()
                .multiply(new BigDecimal("100"))
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();

        Map<String, String> metadata = new HashMap<>();
        metadata.put("packId", String.valueOf(pack.getId()));
        metadata.put("fullName", fullName);
        metadata.put("email", email);
        metadata.put("phone", phone);
        metadata.put("travelersCount", String.valueOf(travelersCount));
        if (username != null && !username.isBlank()) {
            metadata.put("username", username);
        }

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity((long) travelersCount)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency(currency)
                                                .setUnitAmount(unitAmount)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName(pack.getTitle())
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .putAllMetadata(metadata)
                .build();

        try {
            return Session.create(params);
        } catch (StripeException ex) {
            throw new RuntimeException("Stripe checkout session failed", ex);
        }
    }

    public Session retrieveSession(String sessionId) {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("Stripe is not configured. Set STRIPE_SECRET_KEY.");
        }
        Stripe.apiKey = secretKey;
        try {
            return Session.retrieve(sessionId);
        } catch (StripeException ex) {
            throw new RuntimeException("Failed to retrieve Stripe session", ex);
        }
    }

    public Session createFlightCheckoutSession(
            com.example.demo.Flights.Flight flight,
            com.example.demo.Flights.FlightOrder order
    ) {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("Stripe is not configured. Set STRIPE_SECRET_KEY.");
        }
        if (flight == null || flight.getPrice() == null) {
            throw new IllegalArgumentException("Flight price is required");
        }

        Stripe.apiKey = secretKey;
        long unitAmount = flight.getPrice()
                .multiply(new BigDecimal("100"))
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();

        String currencyCode = flight.getCurrency() != null ? flight.getCurrency() : currency;
        currencyCode = currencyCode.toLowerCase();

        Map<String, String> metadata = new HashMap<>();
        if (order != null && order.getId() != null) {
            metadata.put("flightOrderId", String.valueOf(order.getId()));
        }
        if (flight.getOfferId() != null) {
            metadata.put("offerId", flight.getOfferId());
        }

        String title = "Flight " + flight.getOriginAirportCode() + " \u2192 " + flight.getDestinationAirportCode();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(flightSuccessUrl)
                .setCancelUrl(flightCancelUrl)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency(currencyCode)
                                                .setUnitAmount(unitAmount)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName(title)
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .putAllMetadata(metadata)
                .build();

        try {
            return Session.create(params);
        } catch (StripeException ex) {
            throw new RuntimeException("Stripe checkout session failed", ex);
        }
    }
}
