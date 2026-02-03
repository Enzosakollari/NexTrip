package com.example.demo.Controller;

import com.example.demo.Flights.Flight;
import com.example.demo.Flights.FlightOrder;
import com.example.demo.Flights.FlightOrderRepository;
import com.example.demo.Flights.FlightOrderService;
import com.example.demo.Flights.FlightRepository;
import com.example.demo.Flights.FlightSearchCache;
import com.example.demo.Service.StripeService;
import com.example.demo.User.AppUser;
import com.example.demo.User.AppUserRepository;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class FlightBookingController {

    private final FlightRepository flightRepository;
    private final FlightSearchCache flightSearchCache;
    private final FlightOrderRepository flightOrderRepository;
    private final FlightOrderService flightOrderService;
    private final StripeService stripeService;
    private final AppUserRepository appUserRepository;

    @GetMapping("/flights/checkout")
    public String showCheckout(
            @RequestParam String offerId,
            Model model,
            Principal principal
    ) {
        if (principal == null) {
            return "redirect:/login";
        }

        Flight flight = flightSearchCache.getOffer(offerId);
        if (flight == null) {
            flight = flightRepository.findTopByOfferIdOrderByIdDesc(offerId).orElse(null);
        }
        if (flight == null) {
            model.addAttribute("error", "Selected flight offer could not be found. Please search again.");
            return "flights-search";
        }
        if (flight.getRawOfferJson() == null || flight.getRawOfferJson().isBlank()) {
            model.addAttribute("error", "Selected flight offer is no longer available. Please search again.");
            return "flights-search";
        }

        AppUser user = appUserRepository.findByUsername(principal.getName()).orElse(null);
        model.addAttribute("flight", flight);
        model.addAttribute("offerId", offerId);
        model.addAttribute("email", user != null ? user.getEmail() : "");
        model.addAttribute("warning", flight.getAdults() != null && flight.getAdults() > 1
                ? "Only 1 passenger checkout is supported right now. Please search with 1 passenger."
                : null);
        return "flight-checkout";
    }

    @PostMapping("/flights/checkout")
    public String createCheckout(
            @RequestParam String offerId,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String gender,
            @RequestParam String dateOfBirth,
            @RequestParam String email,
            @RequestParam String phoneCountryCode,
            @RequestParam String phone,
            @RequestParam String nationality,
            @RequestParam String documentType,
            @RequestParam String documentNumber,
            @RequestParam String documentExpiry,
            @RequestParam(required = false) String documentIssuanceDate,
            @RequestParam String documentIssuanceCountry,
            Model model,
            Principal principal
    ) {
        if (principal == null) {
            return "redirect:/login";
        }

        Flight flight = flightSearchCache.getOffer(offerId);
        if (flight == null) {
            flight = flightRepository.findTopByOfferIdOrderByIdDesc(offerId).orElse(null);
        }
        if (flight == null) {
            model.addAttribute("error", "Selected flight offer could not be found. Please search again.");
            return "flights-search";
        }
        if (flight.getRawOfferJson() == null || flight.getRawOfferJson().isBlank()) {
            model.addAttribute("error", "Selected flight offer is no longer available. Please search again.");
            return "flights-search";
        }

        if (flight.getAdults() != null && flight.getAdults() > 1) {
            model.addAttribute("flight", flight);
            model.addAttribute("offerId", offerId);
            model.addAttribute("error", "Only 1 passenger checkout is supported right now. Please search with 1 passenger.");
            return "flight-checkout";
        }

        if (isBlank(firstName) || isBlank(lastName) || isBlank(gender) || isBlank(dateOfBirth)
                || isBlank(email) || isBlank(phoneCountryCode) || isBlank(phone)
                || isBlank(nationality) || isBlank(documentType) || isBlank(documentNumber)
                || isBlank(documentExpiry) || isBlank(documentIssuanceCountry)) {
            model.addAttribute("flight", flight);
            model.addAttribute("offerId", offerId);
            model.addAttribute("error", "Please fill in all required fields.");
            return "flight-checkout";
        }

        if (!isTwoLetterCode(nationality) || !isTwoLetterCode(documentIssuanceCountry)) {
            model.addAttribute("flight", flight);
            model.addAttribute("offerId", offerId);
            model.addAttribute("error", "Nationality and document issuance country must be 2-letter codes (e.g., US, FR).");
            return "flight-checkout";
        }

        if (!isAllowedDocumentType(documentType)) {
            model.addAttribute("flight", flight);
            model.addAttribute("offerId", offerId);
            model.addAttribute("error", "Document type must be PASSPORT or ID.");
            return "flight-checkout";
        }

        AppUser user = appUserRepository.findByUsername(principal.getName()).orElse(null);

        LocalDate parsedDob;
        LocalDate parsedExpiry;
        LocalDate parsedIssuanceDate = null;
        try {
            parsedDob = LocalDate.parse(dateOfBirth);
            parsedExpiry = LocalDate.parse(documentExpiry);
            if (documentIssuanceDate != null && !documentIssuanceDate.isBlank()) {
                parsedIssuanceDate = LocalDate.parse(documentIssuanceDate);
            }
        } catch (Exception ex) {
            model.addAttribute("flight", flight);
            model.addAttribute("offerId", offerId);
            model.addAttribute("error", "Please check your date fields (DOB/expiry/issuance).");
            return "flight-checkout";
        }

        FlightOrder order = new FlightOrder();
        order.setOfferId(offerId);
        order.setRawOfferJson(flight.getRawOfferJson());
        order.setAppUser(user);
        order.setEmail(email != null ? email.trim() : email);
        order.setPrice(flight.getPrice());
        order.setCurrency(flight.getCurrency());
        order.setOriginAirportCode(flight.getOriginAirportCode());
        order.setDestinationAirportCode(flight.getDestinationAirportCode());
        order.setDepartureTime(flight.getDepartureTime());
        order.setArrivalTime(flight.getArrivalTime());
        order.setAirline(flight.getAirline());
        order.setFlightNumber(flight.getFlightNumber());
        order.setReturnOriginAirportCode(flight.getReturnOriginAirportCode());
        order.setReturnDestinationAirportCode(flight.getReturnDestinationAirportCode());
        order.setReturnDepartureTime(flight.getReturnDepartureTime());
        order.setReturnArrivalTime(flight.getReturnArrivalTime());
        order.setReturnAirline(flight.getReturnAirline());
        order.setReturnFlightNumber(flight.getReturnFlightNumber());
        order.setFirstName(firstName != null ? firstName.trim().toUpperCase() : firstName);
        order.setLastName(lastName != null ? lastName.trim().toUpperCase() : lastName);
        order.setGender(gender != null ? gender.toUpperCase() : gender);
        order.setDateOfBirth(parsedDob);
        order.setPhoneCountryCode(phoneCountryCode);
        order.setPhone(phone);
        order.setNationality(nationality != null ? nationality.trim().toUpperCase() : nationality);
        order.setDocumentType(documentType != null ? documentType.trim().toUpperCase() : documentType);
        order.setDocumentNumber(documentNumber);
        order.setDocumentExpiry(parsedExpiry);
        if (parsedIssuanceDate != null) {
            order.setDocumentIssuanceDate(parsedIssuanceDate);
        }
        order.setDocumentIssuanceCountry(documentIssuanceCountry != null ? documentIssuanceCountry.trim().toUpperCase() : documentIssuanceCountry);
        order.setPaymentStatus("PENDING");
        order.setOrderStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());

        FlightOrder saved = flightOrderRepository.save(order);
        try {
            Session session = stripeService.createFlightCheckoutSession(flight, saved);
            saved.setStripeSessionId(session.getId());
            flightOrderRepository.save(saved);
            return "redirect:" + session.getUrl();
        } catch (Exception ex) {
            saved.setPaymentStatus("FAILED");
            saved.setOrderStatus("FAILED");
            saved.setErrorMessage(ex.getMessage());
            flightOrderRepository.save(saved);
            model.addAttribute("flight", flight);
            model.addAttribute("offerId", offerId);
            model.addAttribute("error", "Stripe checkout failed: " + ex.getMessage());
            return "flight-checkout";
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean isTwoLetterCode(String value) {
        if (value == null) {
            return false;
        }
        return value.trim().matches("^[A-Za-z]{2}$");
    }

    private boolean isAllowedDocumentType(String value) {
        if (value == null) {
            return false;
        }
        String upper = value.trim().toUpperCase();
        return "PASSPORT".equals(upper) || "ID".equals(upper);
    }

    @GetMapping("/flights/checkout-success")
    public String checkoutSuccess(@RequestParam("session_id") String sessionId, Model model) {
        Session session = stripeService.retrieveSession(sessionId);
        if (!"paid".equalsIgnoreCase(session.getPaymentStatus())) {
            model.addAttribute("error", "Payment not completed.");
            return "flight-booking-cancel";
        }

        FlightOrder order = null;
        if (session.getMetadata() != null && session.getMetadata().containsKey("flightOrderId")) {
            try {
                Long orderId = Long.valueOf(session.getMetadata().get("flightOrderId"));
                order = flightOrderRepository.findById(orderId).orElse(null);
            } catch (NumberFormatException ignored) {
            }
        }
        if (order == null) {
            order = flightOrderRepository.findByStripeSessionId(sessionId).orElse(null);
        }
        if (order == null) {
            model.addAttribute("error", "Flight order not found.");
            return "flight-booking-cancel";
        }

        if (order.getAmadeusOrderId() != null && !order.getAmadeusOrderId().isBlank()) {
            model.addAttribute("order", order);
            return "flight-booking-success";
        }

        try {
            FlightOrderService.FlightOrderResult result = flightOrderService.createOrder(order);
            order.setAmadeusOrderId(result.orderId());
            order.setBookingReference(result.bookingReference());
            order.setRawOrderJson(result.rawJson());
            order.setPaymentStatus("PAID");
            order.setOrderStatus("CONFIRMED");
            flightOrderRepository.save(order);
            model.addAttribute("order", order);
            return "flight-booking-success";
        } catch (Exception ex) {
            order.setPaymentStatus("PAID");
            order.setOrderStatus("FAILED");
            order.setErrorMessage(ex.getMessage());
            flightOrderRepository.save(order);
            model.addAttribute("error", "Payment succeeded but booking failed: " + ex.getMessage());
            return "flight-booking-cancel";
        }
    }

    @GetMapping("/flights/checkout-cancel")
    public String checkoutCancel(@RequestParam(value = "reason", required = false) String reason, Model model) {
        model.addAttribute("error", reason);
        return "flight-booking-cancel";
    }
}
