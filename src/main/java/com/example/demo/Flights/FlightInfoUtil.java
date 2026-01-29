package com.example.demo.Flights;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class FlightInfoUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final Map<String, String> AIRLINE_NAMES = new HashMap<>();

    static {
        AIRLINE_NAMES.put("LH", "Lufthansa");
        AIRLINE_NAMES.put("W6", "Wizz Air");
        AIRLINE_NAMES.put("W4", "Wizz Air Malta");
        AIRLINE_NAMES.put("W9", "Wizz Air UK");
        AIRLINE_NAMES.put("FR", "Ryanair");
        AIRLINE_NAMES.put("U2", "easyJet");
        AIRLINE_NAMES.put("VY", "Vueling");
        AIRLINE_NAMES.put("BA", "British Airways");
        AIRLINE_NAMES.put("AF", "Air France");
        AIRLINE_NAMES.put("KL", "KLM");
        AIRLINE_NAMES.put("IB", "Iberia");
        AIRLINE_NAMES.put("AZ", "ITA Airways");
        AIRLINE_NAMES.put("TK", "Turkish Airlines");
        AIRLINE_NAMES.put("EK", "Emirates");
        AIRLINE_NAMES.put("QR", "Qatar Airways");
        AIRLINE_NAMES.put("EY", "Etihad Airways");
        AIRLINE_NAMES.put("OS", "Austrian Airlines");
        AIRLINE_NAMES.put("LX", "SWISS");
        AIRLINE_NAMES.put("SN", "Brussels Airlines");
        AIRLINE_NAMES.put("SK", "SAS");
        AIRLINE_NAMES.put("AY", "Finnair");
        AIRLINE_NAMES.put("LO", "LOT Polish Airlines");
        AIRLINE_NAMES.put("A3", "Aegean Airlines");
        AIRLINE_NAMES.put("PC", "Pegasus Airlines");
        AIRLINE_NAMES.put("JU", "Air Serbia");
        AIRLINE_NAMES.put("GQ", "Sky Express");
        AIRLINE_NAMES.put("UA", "United Airlines");
        AIRLINE_NAMES.put("AA", "American Airlines");
        AIRLINE_NAMES.put("DL", "Delta Air Lines");
        AIRLINE_NAMES.put("AC", "Air Canada");
        AIRLINE_NAMES.put("NZ", "Air New Zealand");
        AIRLINE_NAMES.put("SQ", "Singapore Airlines");
        AIRLINE_NAMES.put("QF", "Qantas");
        AIRLINE_NAMES.put("CX", "Cathay Pacific");
        AIRLINE_NAMES.put("NH", "All Nippon Airways");
        AIRLINE_NAMES.put("JL", "Japan Airlines");
    }

    private FlightInfoUtil() {
    }

    public static String airlineName(String code) {
        if (code == null || code.isBlank()) {
            return "Airline TBD";
        }
        String upper = code.trim().toUpperCase(Locale.ROOT);
        return AIRLINE_NAMES.getOrDefault(upper, upper);
    }

    public static String airlineDisplay(String code, String flightNumber) {
        String name = airlineName(code);
        if (code == null || code.isBlank()) {
            return name;
        }
        String upper = code.trim().toUpperCase(Locale.ROOT);
        if (flightNumber == null || flightNumber.isBlank()) {
            return upper + " · " + name;
        }
        return upper + " " + flightNumber.trim() + " · " + name;
    }

    public static String checkedBaggageAllowance(String rawOfferJson) {
        return baggageFromOffer(rawOfferJson, "includedCheckedBags");
    }

    public static String cabinBaggageAllowance(String rawOfferJson) {
        String value = baggageFromOffer(rawOfferJson, "includedCabinBags");
        if (!"Not specified".equals(value)) {
            return value;
        }
        return baggageFromOffer(rawOfferJson, "includedHandBags");
    }

    private static String baggageFromOffer(String rawOfferJson, String fieldName) {
        if (rawOfferJson == null || rawOfferJson.isBlank()) {
            return "Not specified";
        }
        try {
            JsonNode root = MAPPER.readTree(rawOfferJson);
            JsonNode traveler = root.path("travelerPricings");
            if (traveler.isArray() && !traveler.isEmpty()) {
                JsonNode firstTraveler = traveler.get(0);
                JsonNode fares = firstTraveler.path("fareDetailsBySegment");
                if (fares.isArray()) {
                    for (JsonNode fare : fares) {
                        JsonNode bags = fare.path(fieldName);
                        String formatted = formatBags(bags);
                        if (!"Not specified".equals(formatted)) {
                            return formatted;
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return "Not specified";
    }

    private static String formatBags(JsonNode bags) {
        if (bags == null || bags.isMissingNode() || bags.isNull()) {
            return "Not specified";
        }
        if (bags.has("weight")) {
            int weight = bags.path("weight").asInt(0);
            String unit = bags.path("weightUnit").asText("KG");
            if (weight > 0) {
                return weight + " " + unit;
            }
        }
        if (bags.has("quantity")) {
            int qty = bags.path("quantity").asInt(0);
            if (qty > 0) {
                return qty + " pc";
            }
        }
        return "Not specified";
    }
}
