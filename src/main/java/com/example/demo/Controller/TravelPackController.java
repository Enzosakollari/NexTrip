package com.example.demo.Controller;

import com.example.demo.Booking.BookingService;
import com.example.demo.Business.TravelPackage;
import com.example.demo.Business.TravelPackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class TravelPackController {

    private final TravelPackageRepository packageRepo;
    private final BookingService bookingService;

    @GetMapping("/travel-packs")
    public String list(
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) String minPrice,
            @RequestParam(required = false) String maxPrice,
            Model model
    ) {
        List<TravelPackage> packs = packageRepo.findAll();
        int totalCount = packs.size();

        String query = normalizeQuery(destination);
        BigDecimal min = parsePrice(minPrice);
        BigDecimal max = parsePrice(maxPrice);

        List<TravelPackage> filtered = packs.stream()
                .filter(pack -> matchesDestination(pack, query))
                .filter(pack -> matchesMinPrice(pack, min))
                .filter(pack -> matchesMaxPrice(pack, max))
                .collect(Collectors.toList());

        filtered.forEach(pack -> pack.setFullyBooked(bookingService.isFullyBooked(pack)));

        model.addAttribute("packs", filtered);
        model.addAttribute("totalPacks", totalCount);
        model.addAttribute("filteredCount", filtered.size());
        model.addAttribute("destinationQuery", destination);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);

        if (priceParseFailed(minPrice) || priceParseFailed(maxPrice)) {
            model.addAttribute("filterError", "Price filters must be valid numbers.");
        }

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

    private String normalizeQuery(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private boolean matchesDestination(TravelPackage pack, String query) {
        if (query.isEmpty()) {
            return true;
        }
        String destination = pack.getDestination() != null
                ? pack.getDestination().toLowerCase(Locale.ROOT)
                : "";
        String title = pack.getTitle() != null
                ? pack.getTitle().toLowerCase(Locale.ROOT)
                : "";
        return destination.contains(query) || title.contains(query);
    }

    private boolean matchesMinPrice(TravelPackage pack, BigDecimal min) {
        if (min == null) {
            return true;
        }
        if (pack.getPrice() == null) {
            return false;
        }
        return pack.getPrice().compareTo(min) >= 0;
    }

    private boolean matchesMaxPrice(TravelPackage pack, BigDecimal max) {
        if (max == null) {
            return true;
        }
        if (pack.getPrice() == null) {
            return false;
        }
        return pack.getPrice().compareTo(max) <= 0;
    }

    private BigDecimal parsePrice(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private boolean priceParseFailed(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            new BigDecimal(value.trim());
            return false;
        } catch (NumberFormatException ex) {
            return true;
        }
    }
}
