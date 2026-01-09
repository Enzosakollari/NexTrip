package com.example.demo.Controller;

import com.example.demo.Booking.BookingService;
import com.example.demo.Booking.BookingStatus;
import com.example.demo.Service.BusinessUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/business")
public class BusinessBookingController {

    private final BookingService bookingService;
    private final BusinessUserService businessService;

    @GetMapping("/bookings")
    public String bookings(Model model, Principal principal) {
        var business = businessService.currentBusiness(principal);
        model.addAttribute("bookings", bookingService.businessBookings(business.getId()));
        return "business-bookings";
    }

    @PostMapping("/bookings/{id}/status")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam BookingStatus status,
            Principal principal
    ) {
        var business = businessService.currentBusiness(principal);
        bookingService.updateStatus(id, business.getId(), status);
        return "redirect:/business/bookings";
    }
}
