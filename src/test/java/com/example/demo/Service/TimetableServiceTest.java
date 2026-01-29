package com.example.demo.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class TimetableServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private TimetableService timetableService;

    @BeforeEach
    void setUp() {
        // Replace the RestTemplate in the service with our mock
        ReflectionTestUtils.setField(timetableService, "restTemplate", restTemplate);
    }

    @Test
    void getCompletion_returnsStations_whenApiCallSucceeds() {
        // Arrange
        String term = "Zurich";
        String mockResponse = """
                [
                  {
                    "label": "Zürich HB",
                    "html": "<span class='bold'>Zürich HB</span>",
                    "iconclass": "sl-icon-type-train"
                  },
                  {
                    "label": "Zürich, Bellevue",
                    "html": "<span class='bold'>Zürich, Bellevue</span>",
                    "iconclass": "sl-icon-type-tram"
                  }
                ]
                """;
        
        ResponseEntity<String> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        
        when(restTemplate.exchange(
                contains("/completion.json"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);
        
        // Act
        List<Map<String, Object>> result = timetableService.getCompletion(term, false, false, false);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Zürich HB", result.get(0).get("label"));
        assertEquals("Zürich, Bellevue", result.get(1).get("label"));
        
        verify(restTemplate).exchange(
                contains("/completion.json"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        );
    }

    @Test
    void getCompletion_returnsEmptyList_whenApiCallFails() {
        // Arrange
        String term = "InvalidStation";
        
        ResponseEntity<String> responseEntity = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        
        when(restTemplate.exchange(
                contains("/completion.json"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);
        
        // Act
        List<Map<String, Object>> result = timetableService.getCompletion(term, false, false, false);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(restTemplate).exchange(
                contains("/completion.json"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        );
    }

    @Test
    void getStationsByCoordinates_returnsStations_whenApiCallSucceeds() {
        // Arrange
        String latlon = "47.378177,8.540192"; // Coordinates for Zurich
        String mockResponse = """
                [
                  {
                    "label": "Zürich HB",
                    "dist": 500,
                    "iconclass": "sl-icon-type-train"
                  },
                  {
                    "label": "Zürich, Central",
                    "dist": 750,
                    "iconclass": "sl-icon-type-tram"
                  }
                ]
                """;
        
        ResponseEntity<String> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        
        when(restTemplate.exchange(
                contains("/completion.json"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);
        
        // Act
        List<Map<String, Object>> result = timetableService.getStationsByCoordinates(latlon, 1000, false, false);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Zürich HB", result.get(0).get("label"));
        assertEquals(500, result.get(0).get("dist"));
        
        verify(restTemplate).exchange(
                contains("/completion.json"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        );
    }

    @Test
    void searchRoute_returnsRoute_whenApiCallSucceeds() {
        // Arrange
        String from = "Zurich";
        String to = "Bern";
        String mockResponse = """
                {
                  "connections": [
                    {
                      "from": "Zürich HB",
                      "to": "Bern",
                      "duration": "00:56",
                      "transfers": 0,
                      "sections": [
                        {
                          "departure": {
                            "station": "Zürich HB",
                            "time": "14:04"
                          },
                          "arrival": {
                            "station": "Bern",
                            "time": "15:00"
                          },
                          "journey": {
                            "name": "IC 1",
                            "type": "train"
                          }
                        }
                      ]
                    }
                  ]
                }
                """;
        
        ResponseEntity<String> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        
        when(restTemplate.exchange(
                contains("/route.json"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);
        
        // Act
        Map<String, Object> result = timetableService.searchRoute(from, to, null, null, null, null, null, null, false, false, false, null, null, false);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("connections"));
        
        verify(restTemplate).exchange(
                contains("/route.json"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        );
    }

    @Test
    void getStation_returnsStationInfo_whenApiCallSucceeds() {
        // Arrange
        String stop = "Zurich HB";
        String mockResponse = """
                {
                  "name": "Zürich HB",
                  "coordinates": {
                    "x": "8.540192",
                    "y": "47.378177"
                  },
                  "id": "8503000"
                }
                """;
        
        ResponseEntity<String> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        
        when(restTemplate.exchange(
                contains("/station.json"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);
        
        // Act
        Map<String, Object> result = timetableService.getStation(stop);
        
        // Assert
        assertNotNull(result);
        assertEquals("Zürich HB", result.get("name"));
        
        verify(restTemplate).exchange(
                contains("/station.json"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        );
    }

    @Test
    void getStationboard_returnsStationboard_whenApiCallSucceeds() {
        // Arrange
        String stop = "Zurich HB";
        String mockResponse = """
                {
                  "station": {
                    "name": "Zürich HB",
                    "id": "8503000"
                  },
                  "stationboard": [
                    {
                      "stop": {
                        "station": "Zürich HB",
                        "time": "14:04"
                      },
                      "name": "IC 1",
                      "to": "Bern"
                    },
                    {
                      "stop": {
                        "station": "Zürich HB",
                        "time": "14:12"
                      },
                      "name": "IR 37",
                      "to": "Basel SBB"
                    }
                  ]
                }
                """;
        
        ResponseEntity<String> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        
        when(restTemplate.exchange(
                contains("/stationboard.json"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);
        
        // Act
        Map<String, Object> result = timetableService.getStationboard(stop, null, null, null, null, false, false, false, false, null);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("station"));
        assertTrue(result.containsKey("stationboard"));
        
        verify(restTemplate).exchange(
                contains("/stationboard.json"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        );
    }
}