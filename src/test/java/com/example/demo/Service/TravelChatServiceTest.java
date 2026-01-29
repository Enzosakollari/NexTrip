package com.example.demo.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class TravelChatServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private TravelChatService travelChatService;

    @BeforeEach
    void setUp() {
        // Set up the service with test values
        ReflectionTestUtils.setField(travelChatService, "huggingFaceApiKey", "test-api-key");
        ReflectionTestUtils.setField(travelChatService, "model", "test-model");
        ReflectionTestUtils.setField(travelChatService, "baseUrl", "https://test-api.com");
        
        // Replace the RestTemplate in the service with our mock
        ReflectionTestUtils.setField(travelChatService, "restTemplate", restTemplate);
    }

    @Test
    void askAssistant_returnsResponse_whenApiCallSucceeds() {
        // Arrange
        String userMessage = "Tell me about Paris";
        String expectedResponse = "Paris is a beautiful city!";
        
        // Create a mock response structure similar to what the API would return
        Map<String, Object> messageContent = new HashMap<>();
        messageContent.put("content", expectedResponse);
        
        Map<String, Object> choice = new HashMap<>();
        choice.put("message", messageContent);
        
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("choices", List.of(choice));
        
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        
        when(restTemplate.exchange(
                contains("/chat/completions"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(responseEntity);
        
        // Act
        String result = travelChatService.askAssistant(userMessage);
        
        // Assert
        assertEquals(expectedResponse, result);
        
        verify(restTemplate).exchange(
                contains("/chat/completions"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        );
    }

    @Test
    void askAssistant_returnsErrorMessage_whenApiCallFails() {
        // Arrange
        String userMessage = "Tell me about Paris";
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
        when(exception.getResponseBodyAsString()).thenReturn("Error message");
        
        when(restTemplate.exchange(
                contains("/chat/completions"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenThrow(exception);
        
        // Act
        String result = travelChatService.askAssistant(userMessage);
        
        // Assert
        assertTrue(result.contains("Sorry, I had a problem talking to the AI service"));
        assertTrue(result.contains("500"));
        
        verify(restTemplate).exchange(
                contains("/chat/completions"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        );
    }

    @Test
    void askAssistant_returnsErrorMessage_whenResponseIsInvalid() {
        // Arrange
        String userMessage = "Tell me about Paris";
        
        // Create an invalid response structure
        Map<String, Object> responseBody = new HashMap<>();
        // Missing the 'choices' field
        
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        
        when(restTemplate.exchange(
                contains("/chat/completions"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(responseEntity);
        
        // Act
        String result = travelChatService.askAssistant(userMessage);
        
        // Assert
        assertEquals("Sorry, I couldn't generate a travel suggestion right now.", result);
        
        verify(restTemplate).exchange(
                contains("/chat/completions"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        );
    }
}