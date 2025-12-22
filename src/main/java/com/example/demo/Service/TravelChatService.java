package com.example.demo.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class TravelChatService {

    private static final Logger log =
            LoggerFactory.getLogger(TravelChatService.class);

    @Value("${huggingface.api.key}")
    private String huggingFaceApiKey;

    @Value("${huggingface.router.model}")
    private String model;

    @Value("${huggingface.router.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public String askAssistant(String userMessage) {
        String url = baseUrl + "/chat/completions";

        List<Map<String, Object>> messages = List.of(
                Map.of(
                        "role", "system",
                        "content", "You are NextTrip, a friendly travel assistant. " +
                                "Give short, concrete travel suggestions (destinations, dates, and tips) in a casual tone."
                ),
                Map.of(
                        "role", "user",
                        "content", userMessage
                )
        );

        Map<String, Object> payload = Map.of(
                "model", model,
                "messages", messages,
                "max_tokens", 220,
                "temperature", 0.7,
                "top_p", 0.95
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(huggingFaceApiKey);

        log.info("Calling HF Router → model={} tokenLoaded={}",
                model,
                huggingFaceApiKey != null && !huggingFaceApiKey.isBlank());

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(payload, headers),
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("HF Router accepted ✅ status={}", response.getStatusCodeValue());
            } else {
                log.warn("HF Router non-2xx ⚠️ status={} body={}",
                        response.getStatusCodeValue(),
                        response.getBody());
            }

            if (response.getBody() == null) {
                return "Sorry, no response from AI service.";
            }

            Map body = response.getBody();
            Object choicesObj = body.get("choices");

            if (choicesObj instanceof List<?> choices && !choices.isEmpty()) {
                Object first = choices.get(0);
                if (first instanceof Map<?, ?> firstChoice) {
                    Object messageObj = firstChoice.get("message");
                    if (messageObj instanceof Map<?, ?> msg) {
                        Object content = msg.get("content");
                        if (content != null) return content.toString().trim();
                    }
                }
            }

            return "Sorry, I couldn't generate a travel suggestion.";

        } catch (HttpStatusCodeException e) {
            log.error("HF Router rejected ❌ status={} body={}",
                    e.getStatusCode().value(),
                    e.getResponseBodyAsString());
            return "AI service rejected the request (" + e.getStatusCode() + ").";
        } catch (Exception e) {
            log.error("HF Router call failed ❌", e);
            return "AI service error.";
        }
    }
}
