package com.example.demo.Controller;

import com.example.demo.Booking.Booking;
import com.example.demo.Booking.BookingService;
import com.example.demo.Booking.BookingStatus;
import com.example.demo.Business.TravelPackageRepository;
import com.example.demo.Flights.FlightInfoUtil;
import com.example.demo.Flights.FlightOrder;
import com.example.demo.Flights.FlightOrderRepository;
import com.example.demo.Service.StripeService;
import com.example.demo.User.AppUser;
import com.example.demo.User.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final TravelPackageRepository travelPackageRepository;
    private final StripeService stripeService;
    private final FlightOrderRepository flightOrderRepository;
    private final AppUserRepository appUserRepository;

    @GetMapping("/packs/{packId}/book")
    public String bookForm(@PathVariable Long packId, Model model) {
        model.addAttribute("packId", packId);
        model.addAttribute("fullyBooked", bookingService.isFullyBooked(packId));
        return "booking-form";
    }

    @PostMapping("/bookings")
    public String submitBooking(
            @RequestParam Long packId,
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam int travelersCount,
            Principal principal
    ) {
        return "redirect:/";
    }

    @GetMapping("/booking-success")
    public String bookingSuccess(@RequestParam("session_id") String sessionId, Model model) {
        var session = stripeService.retrieveSession(sessionId);
        if (!"paid".equalsIgnoreCase(session.getPaymentStatus())) {
            model.addAttribute("error", "Payment not completed.");
            return "booking-cancel";
        }

        var metadata = session.getMetadata();
        if (metadata == null || !metadata.containsKey("packId")) {
            model.addAttribute("error", "Missing booking details.");
            return "booking-cancel";
        }

        Long packId = Long.valueOf(metadata.get("packId"));
        int travelersCount = Integer.parseInt(metadata.getOrDefault("travelersCount", "1"));
        String fullName = metadata.getOrDefault("fullName", "");
        String email = metadata.getOrDefault("email", "");
        String phone = metadata.getOrDefault("phone", "");
        String username = metadata.get("username");

        try {
            bookingService.createBookingAfterPayment(
                    packId,
                    fullName,
                    email,
                    phone,
                    travelersCount,
                    username,
                    sessionId
            );
        } catch (IllegalStateException ex) {
            model.addAttribute("error", ex.getMessage());
            return "booking-cancel";
        }

        return "booking-success";
    }

    @GetMapping("/booking-cancel")
    public String bookingCancel(@RequestParam(value = "reason", required = false) String reason, Model model) {
        model.addAttribute("error", reason);
        return "booking-cancel";
    }

    @PostMapping("/bookings/checkout")
    public String createCheckout(
            @RequestParam Long packId,
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam int travelersCount,
            Principal principal
    ) {
        if (principal == null) {
            return "redirect:/login";
        }

        var pack = travelPackageRepository.findById(packId)
                .orElseThrow(() -> new IllegalArgumentException("Pack not found"));

        if (bookingService.isFullyBooked(pack)) {
            return "redirect:/travel-packs/" + packId + "?error=full";
        }

        String username = principal != null ? principal.getName() : null;
        var session = stripeService.createCheckoutSession(
                pack,
                fullName,
                email,
                phone,
                travelersCount,
                username
        );

        return "redirect:" + session.getUrl();
    }

    @GetMapping("/my-bookings")
    public String myBookings(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        List<Booking> bookings = bookingService.getUserBookings(principal);
        List<Booking> upcomingBookings = bookings.stream()
                .filter(b -> !isHistory(b))
                .toList();
        List<Booking> historyBookings = bookings.stream()
                .filter(this::isHistory)
                .toList();
        model.addAttribute("upcomingBookings", upcomingBookings);
        model.addAttribute("historyBookings", historyBookings);
        return "my-bookings";
    }

    @GetMapping("/my-tickets")
    public String myTickets(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        List<Booking> tickets = bookingService.getUserBookings(principal);
        model.addAttribute("tickets", tickets);
        AppUser user = appUserRepository.findByUsername(principal.getName()).orElse(null);
        List<FlightOrder> planeTickets = user != null
                ? flightOrderRepository.findByAppUserOrderByCreatedAtDesc(user)
                : List.of();
        model.addAttribute("planeTickets", planeTickets);
        model.addAttribute("airlineNames", planeTickets.stream()
                .collect(java.util.stream.Collectors.toMap(
                        FlightOrder::getId,
                        o -> FlightInfoUtil.airlineDisplay(o.getAirline(), o.getFlightNumber())
                )));
        model.addAttribute("returnAirlineNames", planeTickets.stream()
                .collect(java.util.stream.Collectors.toMap(
                        FlightOrder::getId,
                        o -> FlightInfoUtil.airlineDisplay(o.getReturnAirline(), o.getReturnFlightNumber())
                )));
        model.addAttribute("checkedBags", planeTickets.stream()
                .collect(java.util.stream.Collectors.toMap(
                        FlightOrder::getId,
                        o -> FlightInfoUtil.checkedBaggageAllowance(o.getRawOfferJson())
                )));
        model.addAttribute("cabinBags", planeTickets.stream()
                .collect(java.util.stream.Collectors.toMap(
                        FlightOrder::getId,
                        o -> FlightInfoUtil.cabinBaggageAllowance(o.getRawOfferJson())
                )));
        return "my-tickets";
    }

    private boolean isHistory(Booking booking) {
        if (booking == null) {
            return false;
        }
        BookingStatus status = booking.getStatus();
        return status == BookingStatus.CANCELLED || status == BookingStatus.COMPLETED;
    }
}
