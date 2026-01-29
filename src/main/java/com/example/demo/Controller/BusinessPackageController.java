package com.example.demo.Controller;

import com.example.demo.Business.TravelPackage;
import com.example.demo.Service.BusinessUserService;
import com.example.demo.Service.FileStorageService;
import com.example.demo.User.BusinessUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/business")
public class BusinessPackageController {

    private final BusinessUserService businessService;
    private final FileStorageService fileStorageService;

    @GetMapping("/packs")
    public String myPacks(Model model, Principal principal) {
        BusinessUser business = businessService.currentBusiness(principal);
        if (business == null) return "redirect:/business/login";

        model.addAttribute("packs", businessService.getMyPackages(business.getId()));
        model.addAttribute("business", business);
        return "business-packs";
    }

    @GetMapping("/packs/new")
    public String newPack(Model model, Principal principal) {
        BusinessUser business = businessService.currentBusiness(principal);
        if (business == null) return "redirect:/business/login";

        model.addAttribute("business", business);
        model.addAttribute("pack", new TravelPackage());
        model.addAttribute("mode", "create");
        return "business-pack-form";
    }

    @PostMapping("/packs")
    public String create(@ModelAttribute TravelPackage pack,
                         @RequestParam(value = "image", required = false) MultipartFile image,
                         @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail,
                         @RequestParam(value = "galleryFiles", required = false) MultipartFile[] galleryFiles,
                         Principal principal) {
        BusinessUser business = businessService.currentBusiness(principal);
        if (business == null) return "redirect:/business/login";

        String imageUrl = fileStorageService.storePackImage(image);
        if (imageUrl != null) {
            pack.setImageUrl(imageUrl);
        }
        String thumbnailUrl = fileStorageService.storePackImage(thumbnail);
        if (thumbnailUrl != null) {
            pack.setThumbnailUrl(thumbnailUrl);
        }
        var galleryUrls = fileStorageService.storePackImages(galleryFiles);
        if (!galleryUrls.isEmpty()) {
            pack.setGalleryImages(String.join(",", galleryUrls));
        }
        businessService.createPackage(business.getId(), pack);
        return "redirect:/business/packs";
    }

    @GetMapping("/packs/{packId}/edit")
    public String edit(@PathVariable Long packId, Model model, Principal principal) {
        BusinessUser business = businessService.currentBusiness(principal);
        if (business == null) return "redirect:/business/login";

        model.addAttribute("business", business);
        model.addAttribute("pack", businessService.getPackageForBusiness(business.getId(), packId));
        model.addAttribute("mode", "edit");
        return "business-pack-form";
    }

    @PostMapping("/packs/{packId}")
    public String update(@PathVariable Long packId,
                         @ModelAttribute TravelPackage pack,
                         @RequestParam(value = "image", required = false) MultipartFile image,
                         @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail,
                         @RequestParam(value = "galleryFiles", required = false) MultipartFile[] galleryFiles,
                         Principal principal) {
        BusinessUser business = businessService.currentBusiness(principal);
        if (business == null) return "redirect:/business/login";

        String imageUrl = fileStorageService.storePackImage(image);
        if (imageUrl != null) {
            pack.setImageUrl(imageUrl);
        }
        String thumbnailUrl = fileStorageService.storePackImage(thumbnail);
        if (thumbnailUrl != null) {
            pack.setThumbnailUrl(thumbnailUrl);
        }
        var galleryUrls = fileStorageService.storePackImages(galleryFiles);
        if (!galleryUrls.isEmpty()) {
            pack.setGalleryImages(String.join(",", galleryUrls));
        }
        businessService.updatePackage(business.getId(), packId, pack);
        return "redirect:/business/packs";
    }

    @PostMapping("/packs/{packId}/delete")
    public String delete(@PathVariable Long packId, Principal principal) {
        BusinessUser business = businessService.currentBusiness(principal);
        if (business == null) return "redirect:/business/login";

        businessService.deletePackage(business.getId(), packId);
        return "redirect:/business/packs";
    }
}
