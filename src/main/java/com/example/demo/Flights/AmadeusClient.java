package com.example.demo.Flights;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@Component
public class AmadeusClient {

    @Value("${amadeus.api.base-url}")
    private String baseUrl;

    @Value("${amadeus.api.key}")
    private String clientId;

    @Value("${amadeus.api.secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    @Getter
    private String accessToken;
    private Instant tokenExpiry = Instant.EPOCH;

    private synchronized void refreshTokenIfNeeded() {
        // still valid for at least 60 seconds
        if (accessToken != null && Instant.now().isBefore(tokenExpiry.minusSeconds(60))) {
            return;
        }

        String url = baseUrl + "/v1/security/oauth2/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "grant_type=client_credentials"
                + "&client_id=" + clientId
                + "&client_secret=" + clientSecret;

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to get Amadeus token: " + response.getStatusCode());
        }

        try {
            JsonNode root = mapper.readTree(response.getBody());
            this.accessToken = root.path("access_token").asText();
            long expiresIn = root.path("expires_in").asLong(1800); // seconds
            this.tokenExpiry = Instant.now().plusSeconds(expiresIn);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Amadeus token response", e);
        }
    }

    public String getValidAccessToken() {
        refreshTokenIfNeeded();
        return accessToken;
    }
}
