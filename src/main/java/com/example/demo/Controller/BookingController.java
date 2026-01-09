package com.example.demo.Controller;

import com.example.demo.Booking.Booking;
import com.example.demo.Booking.BookingService;
import com.example.demo.Business.TravelPackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping("/packs/{packId}/book")
    public String bookForm(@PathVariable Long packId, Model model) {
        model.addAttribute("packId", packId);
        return "booking-form"; // or return something else for now
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
        bookingService.createBooking(packId, fullName, email, phone, travelersCount, principal);
        return "redirect:/booking-success";
    }

    @GetMapping("/booking-success")
    public String bookingSuccess() {
        return "booking-success";
    }
}

