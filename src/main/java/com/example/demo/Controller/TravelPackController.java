package com.example.demo.Controller;

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

    @GetMapping("/travel-packs")
    public String list(Model model) {
        model.addAttribute("packs", packageRepo.findAll());
        return "travel-packs";
    }


    @GetMapping("/travel-packs/{id}")
    public String details(@PathVariable Long id, Model model) {
        TravelPackage pack = packageRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Package not found"));
        model.addAttribute("pack", pack);
        return "travel-pack-details";
    }
}
