package com.example.demo.Controller;

import com.example.demo.User.BusinessUser;
import com.example.demo.User.BusinessUserRepository;
import com.example.demo.utils.JwtTokenUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class BusinessVerificationController {

    private final BusinessUserRepository businessUserRepository;
    private final JwtTokenUtil jwtTokenUtil;

    public BusinessVerificationController(BusinessUserRepository businessUserRepository,
                                          JwtTokenUtil jwtTokenUtil) {
        this.businessUserRepository = businessUserRepository;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @GetMapping("/req/business/verify")
    public String verifyBusinessEmail(@RequestParam("token") String token,
                                      RedirectAttributes redirectAttributes) {
        try {
            String email = jwtTokenUtil.extractEmail(token);
            BusinessUser user = businessUserRepository.findByEmail(email);

            if (user == null || user.getVerificationToken() == null) {
                redirectAttributes.addFlashAttribute("error", "Token Expired or Invalid");
                return "redirect:/business/login";
            }

            if (!jwtTokenUtil.validateToken(token) || !user.getVerificationToken().equals(token)) {
                redirectAttributes.addFlashAttribute("error", "Token Expired or Invalid");
                return "redirect:/business/login";
            }

            user.setVerificationToken(null);
            user.setVerified(true);
            businessUserRepository.save(user);

            redirectAttributes.addFlashAttribute("message",
                    "Business email verified successfully! You can now login.");
            return "redirect:/business/login";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "An error occurred during verification. Please try again.");
            return "redirect:/business/login";
        }
    }

    @GetMapping("/req/business/verify-api")
    @ResponseBody
    public ResponseEntity<String> verifyBusinessEmailApi(@RequestParam("token") String token) {
        try {
            String email = jwtTokenUtil.extractEmail(token);
            BusinessUser user = businessUserRepository.findByEmail(email);

            if (user == null || user.getVerificationToken() == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token Expired or Invalid");
            }

            if (!jwtTokenUtil.validateToken(token) || !user.getVerificationToken().equals(token)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token Expired or Invalid");
            }

            user.setVerificationToken(null);
            user.setVerified(true);
            businessUserRepository.save(user);

            return ResponseEntity.status(HttpStatus.CREATED).body("Business email Verified successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred during verification.");
        }
    }
}
