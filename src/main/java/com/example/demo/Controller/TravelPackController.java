package com.example.demo.Controller;

import com.example.demo.Booking.BookingService;
import com.example.demo.Business.TravelPackage;
import com.example.demo.Business.TravelPackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class TravelPackController {

    private final TravelPackageRepository packageRepo;
    private final BookingService bookingService;

    @GetMapping("/travel-packs")
    public String list(Model model) {
        var packs = packageRepo.findAll();
        packs.forEach(pack -> pack.setFullyBooked(bookingService.isFullyBooked(pack)));
        model.addAttribute("packs", packs);
        return "travel-packs";
    }


    @GetMapping("/travel-packs/{id}")
    public String details(@PathVariable Long id, Model model) {
        TravelPackage pack = packageRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Package not found"));
        pack.setFullyBooked(bookingService.isFullyBooked(pack));
        model.addAttribute("pack", pack);
        return "travel-pack-details";
    }
}
