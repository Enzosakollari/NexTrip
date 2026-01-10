package com.example.demo.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TimetableService {

    private static final String BASE_URL = "https://search.ch/timetable/api";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Get station completion/autocomplete suggestions
     */
    public List<Map<String, Object>> getCompletion(String term, Boolean nofavorites, Boolean showIds, Boolean showCoordinates) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(BASE_URL + "/completion.json")
                    .queryParam("term", term);
            
            if (nofavorites != null && nofavorites) {
                builder.queryParam("nofavorites", "1");
            }
            if (showIds != null && showIds) {
                builder.queryParam("show_ids", "1");
            }
            if (showCoordinates != null && showCoordinates) {
                builder.queryParam("show_coordinates", "1");
            }

            ResponseEntity<String> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    new HttpEntity<>(new HttpHeaders()),
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = mapper.readTree(response.getBody());
                List<Map<String, Object>> results = new ArrayList<>();
                if (root.isArray()) {
                    for (JsonNode item : root) {
                        Map<String, Object> result = new HashMap<>();
                        if (item.has("label")) result.put("label", item.get("label").asText());
                        if (item.has("html")) result.put("html", item.get("html").asText());
                        if (item.has("iconclass")) result.put("iconclass", item.get("iconclass").asText());
                        if (item.has("id")) result.put("id", item.get("id").asText());
                        if (item.has("x")) result.put("x", item.get("x").asText());
                        if (item.has("y")) result.put("y", item.get("y").asText());
                        results.add(result);
                    }
                }
                return results;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * Get stations by coordinates
     */
    public List<Map<String, Object>> getStationsByCoordinates(String latlon, Integer accuracy, Boolean showIds, Boolean showCoordinates) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(BASE_URL + "/completion.json")
                    .queryParam("latlon", latlon);
            
            if (accuracy != null) {
                builder.queryParam("accuracy", accuracy);
            }
            if (showIds != null && showIds) {
                builder.queryParam("show_ids", "1");
            }
            if (showCoordinates != null && showCoordinates) {
                builder.queryParam("show_coordinates", "1");
            }

            ResponseEntity<String> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    new HttpEntity<>(new HttpHeaders()),
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = mapper.readTree(response.getBody());
                List<Map<String, Object>> results = new ArrayList<>();
                if (root.isArray()) {
                    for (JsonNode item : root) {
                        Map<String, Object> result = new HashMap<>();
                        if (item.has("label")) result.put("label", item.get("label").asText());
                        if (item.has("dist")) result.put("dist", item.get("dist").asInt());
                        if (item.has("iconclass")) result.put("iconclass", item.get("iconclass").asText());
                        if (item.has("id")) result.put("id", item.get("id").asText());
                        if (item.has("x")) result.put("x", item.get("x").asText());
                        if (item.has("y")) result.put("y", item.get("y").asText());
                        results.add(result);
                    }
                }
                return results;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * Search for routes
     */
    public Map<String, Object> searchRoute(String from, String to, String via, String date, String time,
                                           String timeType, Integer num, Integer pre, Boolean showDelays,
                                           Boolean showTrackChanges, Boolean oneToMany, Integer interestDuration,
                                           String transportationTypes, Boolean summary) {
        try {
            // Normalize station names - Spring already decodes URL parameters
            // Remove extra spaces and ensure proper format
            String normalizedFrom = from.trim().replaceAll("\\s+", " ");
            String normalizedTo = to.trim().replaceAll("\\s+", " ");
            
            // Try to get the exact station name from completion API if the search fails
            // This helps with station name variations
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(BASE_URL + "/route.json")
                    .queryParam("from", normalizedFrom)
                    .queryParam("to", normalizedTo);
            
            if (via != null && !via.isEmpty()) {
                String normalizedVia = via.trim().replaceAll("\\s+", " ");
                builder.queryParam("via", normalizedVia);
            }
            if (date != null && !date.isEmpty()) {
                builder.queryParam("date", date);
            }
            if (time != null && !time.isEmpty()) {
                builder.queryParam("time", time);
            }
            if (timeType != null && !timeType.isEmpty()) {
                builder.queryParam("time_type", timeType);
            }
            if (num != null) {
                builder.queryParam("num", num);
            }
            if (pre != null) {
                builder.queryParam("pre", pre);
            }
            if (showDelays != null && showDelays) {
                builder.queryParam("show_delays", "1");
            }
            if (showTrackChanges != null && showTrackChanges) {
                builder.queryParam("show_trackchanges", "1");
            }
            if (oneToMany != null && oneToMany) {
                builder.queryParam("one_to_many", "1");
            }
            if (interestDuration != null) {
                builder.queryParam("interest_duration", interestDuration);
            }
            if (transportationTypes != null && !transportationTypes.isEmpty()) {
                builder.queryParam("transportation_types", transportationTypes);
            }
            if (summary != null && summary) {
                builder.queryParam("summary", "1");
            }

            ResponseEntity<String> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    new HttpEntity<>(new HttpHeaders()),
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = mapper.readTree(response.getBody());
                
                // Check if there's an error message about station not found
                if (root.has("messages")) {
                    JsonNode messages = root.path("messages");
                    if (messages.isArray() && messages.size() > 0) {
                        String errorMsg = messages.get(0).asText("");
                        // If station not found, try to resolve using completion API
                        if (errorMsg.contains("nicht gefunden") || errorMsg.contains("not found")) {
                            // Try to get the correct station name from completion API
                            String resolvedFrom = resolveStationName(normalizedFrom);
                            String resolvedTo = resolveStationName(normalizedTo);
                            
                            if (!resolvedFrom.equals(normalizedFrom) || !resolvedTo.equals(normalizedTo)) {
                                // Retry with resolved station names
                                builder = UriComponentsBuilder.fromUriString(BASE_URL + "/route.json")
                                        .queryParam("from", resolvedFrom)
                                        .queryParam("to", resolvedTo);
                                
                                if (date != null && !date.isEmpty()) {
                                    builder.queryParam("date", date);
                                }
                                if (time != null && !time.isEmpty()) {
                                    builder.queryParam("time", time);
                                }
                                if (num != null) {
                                    builder.queryParam("num", num);
                                }
                                
                                response = restTemplate.exchange(
                                        builder.toUriString(),
                                        HttpMethod.GET,
                                        new HttpEntity<>(new HttpHeaders()),
                                        String.class
                                );
                                
                                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                                    root = mapper.readTree(response.getBody());
                                }
                            }
                        }
                    }
                }
                
                @SuppressWarnings("unchecked")
                Map<String, Object> result = mapper.convertValue(root, Map.class);
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }
    
    /**
     * Resolve station name using completion API to get the exact format
     */
    private String resolveStationName(String stationName) {
        try {
            List<Map<String, Object>> suggestions = getCompletion(stationName, false, false, false);
            if (!suggestions.isEmpty()) {
                // Return the label from the first suggestion
                Object label = suggestions.get(0).get("label");
                if (label != null) {
                    return label.toString();
                }
            }
        } catch (Exception e) {
            // If completion fails, return original name
        }
        return stationName;
    }

    /**
     * Get station information
     */
    public Map<String, Object> getStation(String stop) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(BASE_URL + "/station.json")
                    .queryParam("stop", stop);

            ResponseEntity<String> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    new HttpEntity<>(new HttpHeaders()),
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = mapper.readTree(response.getBody());
                @SuppressWarnings("unchecked")
                Map<String, Object> result = mapper.convertValue(root, Map.class);
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    /**
     * Get departure/arrival table for a station
     */
    public Map<String, Object> getStationboard(String stop, String date, String time, String mode,
                                               Integer limit, Boolean showTracks, Boolean showSubsequentStops,
                                               Boolean showDelays, Boolean showTrackChanges, String transportationTypes) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(BASE_URL + "/stationboard.json")
                    .queryParam("stop", stop);
            
            if (date != null && !date.isEmpty()) {
                builder.queryParam("date", date);
            }
            if (time != null && !time.isEmpty()) {
                builder.queryParam("time", time);
            }
            if (mode != null && !mode.isEmpty()) {
                builder.queryParam("mode", mode);
            }
            if (limit != null) {
                builder.queryParam("limit", limit);
            }
            if (showTracks != null && showTracks) {
                builder.queryParam("show_tracks", "1");
            }
            if (showSubsequentStops != null && showSubsequentStops) {
                builder.queryParam("show_subsequent_stops", "1");
            }
            if (showDelays != null && showDelays) {
                builder.queryParam("show_delays", "1");
            }
            if (showTrackChanges != null && showTrackChanges) {
                builder.queryParam("show_trackchanges", "1");
            }
            if (transportationTypes != null && !transportationTypes.isEmpty()) {
                builder.queryParam("transportation_types", transportationTypes);
            }

            ResponseEntity<String> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    new HttpEntity<>(new HttpHeaders()),
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = mapper.readTree(response.getBody());
                @SuppressWarnings("unchecked")
                Map<String, Object> result = mapper.convertValue(root, Map.class);
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }
}
