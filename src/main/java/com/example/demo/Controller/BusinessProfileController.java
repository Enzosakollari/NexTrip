package com.example.demo.Controller;

import com.example.demo.Service.FileStorageService;
import com.example.demo.User.BusinessUser;
import com.example.demo.User.BusinessUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class BusinessProfileController {

    private final BusinessUserRepository businessUserRepository;
    private final FileStorageService fileStorageService;

    @GetMapping("/business/profile")
    public String profile(Principal principal) {
        if (principal == null) {
            return "redirect:/business/login";
        }

        BusinessUser business = businessUserRepository.findByUsername(principal.getName())
                .orElse(null);
        if (business == null) {
            return "redirect:/business/login";
        }

        return "redirect:/business/dashboard#profile";
    }

    @PostMapping("/business/profile")
    public String updateProfile(@RequestParam(value = "description", required = false) String description,
                                @RequestParam(value = "logo", required = false) MultipartFile logo,
                                @RequestParam(value = "banner", required = false) MultipartFile banner,
                                Principal principal) {
        if (principal == null) {
            return "redirect:/business/login";
        }

        BusinessUser business = businessUserRepository.findByUsername(principal.getName())
                .orElse(null);
        if (business == null) {
            return "redirect:/business/login";
        }

        if (description != null) {
            business.setDescription(description.trim());
        }

        String logoUrl = fileStorageService.storeBusinessImage(logo);
        if (logoUrl != null) {
            business.setLogoUrl(logoUrl);
        }

        String bannerUrl = fileStorageService.storeBusinessImage(banner);
        if (bannerUrl != null) {
            business.setBannerUrl(bannerUrl);
        }

        businessUserRepository.save(business);
        return "redirect:/business/dashboard#profile";
    }
}
