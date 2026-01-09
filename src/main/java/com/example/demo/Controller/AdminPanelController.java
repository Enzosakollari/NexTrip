package com.example.demo.Controller;

import com.example.demo.Service.AdminPanelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminPanelController {

    private final AdminPanelService adminPanelService;

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("active", "dashboard");
        model.addAttribute("usersCount", adminPanelService.usersCount());
        model.addAttribute("businessesCount", adminPanelService.businessesCount());
        model.addAttribute("packsCount", adminPanelService.packsCount());
        model.addAttribute("bookingsCount", adminPanelService.bookingsCount());
        return "admin-dashboard";
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("active", "users");
        model.addAttribute("users", adminPanelService.allUsers());
        return "admin-users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        adminPanelService.deleteUser(id);
        return "redirect:/admin/users";
    }

    @GetMapping("/businesses")
    public String businesses(Model model) {
        model.addAttribute("active", "businesses");
        model.addAttribute("businesses", adminPanelService.allBusinesses());
        return "admin-businesses";
    }

    @PostMapping("/businesses/{id}/delete")
    public String deleteBusiness(@PathVariable Long id) {
        adminPanelService.deleteBusiness(id);
        return "redirect:/admin/businesses";
    }

    @GetMapping("/packs")
    public String packs(Model model) {
        model.addAttribute("active", "packs");
        model.addAttribute("packs", adminPanelService.allPacks());
        return "admin-packs";
    }

    @PostMapping("/packs/{id}/delete")
    public String deletePack(@PathVariable Long id) {
        adminPanelService.deletePack(id);
        return "redirect:/admin/packs";
    }

    @GetMapping("/bookings")
    public String bookings(Model model) {
        model.addAttribute("active", "bookings");
        model.addAttribute("bookings", adminPanelService.allBookings());
        return "admin-bookings";
    }

    @PostMapping("/bookings/{id}/delete")
    public String deleteBooking(@PathVariable Long id) {
        adminPanelService.deleteBooking(id);
        return "redirect:/admin/bookings";
    }
}
