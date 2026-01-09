package com.example.demo.Controller;

import com.example.demo.Business.TravelPackageRepository;
import com.example.demo.User.BusinessUser;
import com.example.demo.User.BusinessUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class BusinessDashboardController {

    private final BusinessUserRepository businessUserRepository;
    private final TravelPackageRepository travelPackageRepository;

    @GetMapping("/business/dashboard")
    public String dashboard(Model model, Principal principal) {

        if (principal == null) return "redirect:/business/login";

        String username = principal.getName(); // username
        BusinessUser business = businessUserRepository.findByUsername(username)
                .orElse(null);

        if (business == null) return "redirect:/business/login?error=true";

        model.addAttribute("business", business);
        model.addAttribute("packs",
                travelPackageRepository.findByBusinessUserId(business.getId()));

        return "business-dashboard";
    }
}
