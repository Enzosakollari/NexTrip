package com.example.demo.Controller;

import com.example.demo.Booking.BookingService;
import com.example.demo.Business.TravelPackageRepository;
import com.example.demo.User.BusinessUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class CompanyProfileController {

    private final BusinessUserRepository businessUserRepository;
    private final TravelPackageRepository travelPackageRepository;
    private final BookingService bookingService;

    @GetMapping("/companies/{id}")
    public String companyProfile(@PathVariable Long id, Model model) {
        var business = businessUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Business not found"));

        var packs = travelPackageRepository.findByBusinessUserId(id);
        packs.forEach(pack -> pack.setFullyBooked(bookingService.isFullyBooked(pack)));

        model.addAttribute("business", business);
        model.addAttribute("packs", packs);
        return "company-profile";
    }
}
