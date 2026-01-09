package com.example.demo.Controller;

import com.example.demo.Service.BusinessUserService;
import com.example.demo.Service.TravelChatService;
import com.example.demo.User.BusinessUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/business")
public class BusinessAiController {

    private final BusinessUserService businessService;
    private final TravelChatService travelChatService;

    @GetMapping("/ai")
    public String aiPage(Model model, Principal principal) {
        BusinessUser business = businessService.currentBusiness(principal);
        if (business == null) return "redirect:/business/login";

        model.addAttribute("business", business);
        model.addAttribute("reply", null);
        model.addAttribute("message", "");
        return "business-ai";
    }

    @PostMapping("/ai")
    public String askAi(@RequestParam String message,
                        Model model,
                        Principal principal) {

        BusinessUser business = businessService.currentBusiness(principal);
        if (business == null) return "redirect:/business/login";

        String reply = travelChatService.askAssistant(message);

        model.addAttribute("business", business);
        model.addAttribute("message", message);
        model.addAttribute("reply", reply);
        return "business-ai"; // ❗ NOT redirect
    }
}
