package com.example.demo.Flights;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class FlightOrderService {

    private final AmadeusClient amadeusClient;

    @Value("${amadeus.api.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    public FlightOrderResult createOrder(FlightOrder order) {
        if (order == null || order.getRawOfferJson() == null || order.getRawOfferJson().isBlank()) {
            throw new IllegalArgumentException("Missing flight offer data for order.");
        }

        try {
            JsonNode offerNode = mapper.readTree(order.getRawOfferJson());

            ObjectNode root = mapper.createObjectNode();
            ObjectNode data = root.putObject("data");
            data.put("type", "flight-order");
            ArrayNode offers = data.putArray("flightOffers");
            offers.add(offerNode);

            ArrayNode travelers = data.putArray("travelers");
            ObjectNode traveler = travelers.addObject();
            traveler.put("id", "1");
            traveler.put("dateOfBirth", order.getDateOfBirth().toString());
            traveler.put("gender", normalizeGender(order.getGender()));

            ObjectNode name = traveler.putObject("name");
            name.put("firstName", order.getFirstName());
            name.put("lastName", order.getLastName());

            ObjectNode contact = traveler.putObject("contact");
            contact.put("emailAddress", order.getEmail());
            if (order.getPhone() != null && !order.getPhone().isBlank()) {
                ArrayNode phones = contact.putArray("phones");
                ObjectNode phone = phones.addObject();
                phone.put("deviceType", "MOBILE");
                phone.put("countryCallingCode", sanitizeCountryCode(order.getPhoneCountryCode()));
                phone.put("number", sanitizePhone(order.getPhone()));
            }

            if (order.getDocumentType() != null && !order.getDocumentType().isBlank()) {
                ArrayNode documents = traveler.putArray("documents");
                ObjectNode document = documents.addObject();
                document.put("documentType", order.getDocumentType());
                document.put("number", order.getDocumentNumber());
                document.put("expiryDate", order.getDocumentExpiry().toString());
                document.put("issuanceCountry", order.getDocumentIssuanceCountry());
                document.put("nationality", order.getNationality());
                document.put("holder", true);
                if (order.getDocumentIssuanceDate() != null) {
                    document.put("issuanceDate", order.getDocumentIssuanceDate().toString());
                }
            }

            String url = baseUrl + "/v1/booking/flight-orders";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(amadeusClient.getValidAccessToken());
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(mapper.writeValueAsString(root), headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Amadeus order failed: " + response.getStatusCode());
            }

            JsonNode responseRoot = mapper.readTree(response.getBody());
            JsonNode dataNode = responseRoot.path("data");
            String orderId = dataNode.path("id").asText("");
            String bookingReference = "";
            JsonNode associated = dataNode.path("associatedRecords");
            if (associated.isArray() && associated.size() > 0) {
                bookingReference = associated.get(0).path("reference").asText("");
            }

            return new FlightOrderResult(orderId, bookingReference, responseRoot.toString());
        } catch (Exception ex) {
            throw new RuntimeException("Failed to create flight order: " + ex.getMessage(), ex);
        }
    }

    private String normalizeGender(String value) {
        if (value == null) {
            return "MALE";
        }
        String trimmed = value.trim().toUpperCase();
        if (trimmed.startsWith("F")) {
            return "FEMALE";
        }
        return "MALE";
    }

    private String sanitizeCountryCode(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("+", "").replaceAll("\\D", "");
    }

    private String sanitizePhone(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("\\D", "");
    }

    public record FlightOrderResult(String orderId, String bookingReference, String rawJson) {}
}
