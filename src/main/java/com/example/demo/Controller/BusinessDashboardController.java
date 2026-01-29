package com.example.demo.Controller;

import com.example.demo.Booking.BookingRepository;
import com.example.demo.Booking.BookingStatus;
import com.example.demo.Business.TravelPackageRepository;
import com.example.demo.User.BusinessUser;
import com.example.demo.User.BusinessUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class BusinessDashboardController {

    private final BusinessUserRepository businessUserRepository;
    private final TravelPackageRepository travelPackageRepository;
    private final BookingRepository bookingRepository;

    @GetMapping("/business/dashboard")
    public String dashboard(Model model, Principal principal) {

        if (principal == null) return "redirect:/business/login";

        String username = principal.getName(); // username
        BusinessUser business = businessUserRepository.findByUsername(username)
                .orElse(null);

        if (business == null) return "redirect:/business/login?error=true";

        var packs = travelPackageRepository.findByBusinessUserId(business.getId());
        var bookings = bookingRepository.findAllByBusinessId(business.getId());
        var earnings = bookingRepository.sumEarningsByBusinessId(business.getId());
        int totalBookings = 0;
        int totalTickets = 0;
        Map<Long, Integer> packTickets = new HashMap<>();
        for (var booking : bookings) {
            if (booking.getStatus() == BookingStatus.CANCELLED) {
                continue;
            }
            int travelers = booking.getTravelersCount();
            totalBookings += 1;
            totalTickets += travelers;
            Long packId = booking.getTravelPackage().getId();
            packTickets.merge(packId, travelers, Integer::sum);
        }
        Map<Long, BigDecimal> packEarnings = new HashMap<>();
        for (Object[] row : bookingRepository.sumEarningsByPackForBusiness(business.getId())) {
            Long packId = (Long) row[0];
            BigDecimal total = (BigDecimal) row[1];
            packEarnings.put(packId, total == null ? BigDecimal.ZERO : total);
        }
        for (var pack : packs) {
            packEarnings.putIfAbsent(pack.getId(), BigDecimal.ZERO);
        }
        Long popularPackId = null;
        int popularTickets = 0;
        for (var entry : packTickets.entrySet()) {
            if (entry.getValue() != null && entry.getValue() > popularTickets) {
                popularTickets = entry.getValue();
                popularPackId = entry.getKey();
            }
        }
        final Long popularPackIdFinal = popularPackId;
        var popularPack = packs.stream()
                .filter(p -> p.getId().equals(popularPackIdFinal))
                .findFirst()
                .orElse(null);

        String currency = packs.stream()
                .map(p -> p.getCurrency())
                .filter(c -> c != null && !c.isBlank())
                .findFirst()
                .orElse("EUR");

        model.addAttribute("business", business);
        model.addAttribute("packs", packs);
        model.addAttribute("earnings", earnings);
        model.addAttribute("earningsCurrency", currency);
        model.addAttribute("packEarnings", packEarnings);
        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("totalTickets", totalTickets);
        model.addAttribute("popularPack", popularPack);
        model.addAttribute("popularPackTickets", popularTickets);

        return "business-dashboard";
    }
}
