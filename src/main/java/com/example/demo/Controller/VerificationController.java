package com.example.demo.Controller;


import com.example.demo.User.AppUser;
import com.example.demo.User.AppUserRepository;
import com.example.demo.User.AppUserService;
import com.example.demo.utils.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class VerificationController {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @GetMapping("/req/signup/verify")
    public String verifyEmail(@RequestParam("token") String token, RedirectAttributes redirectAttributes) {
        try {
            String emailString = jwtTokenUtil.extractEmail(token);
            AppUser appuser = appUserRepository.findByEmail(emailString);

            if (appuser == null || appuser.getVerificationToken() == null) {
                redirectAttributes.addFlashAttribute("error", "Token Expired or Invalid");
                return "redirect:/login";
            }

            if (!jwtTokenUtil.validateToken(token) || !appuser.getVerificationToken().equals(token)) {
                redirectAttributes.addFlashAttribute("error", "Token Expired or Invalid");
                return "redirect:/login";
            }

            appuser.setVerificationToken(null);
            appuser.setVerified(true);
            appUserRepository.save(appuser);

            redirectAttributes.addFlashAttribute("message", "Email verified successfully! You can now login.");
            System.out.println("Email verification successful for: " + emailString);
            return "redirect:/login";
        } catch (Exception e) {
            System.err.println("Error during email verification: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "An error occurred during verification. Please try again.");
            return "redirect:/login";
        }
    }

    @GetMapping("/req/signup/verify-api")
    @ResponseBody
    public ResponseEntity<String> verifyEmailApi(@RequestParam("token") String token) {
        String emailString = jwtTokenUtil.extractEmail(token);
        AppUser appuser = appUserRepository.findByEmail(emailString);

        if (appuser == null || appuser.getVerificationToken() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token Expired or Invalid");
        }

        if (!jwtTokenUtil.validateToken(token) || !appuser.getVerificationToken().equals(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token Expired or Invalid");
        }

        appuser.setVerificationToken(null);
        appuser.setVerified(true);
        appUserRepository.save(appuser);

        return ResponseEntity.status(HttpStatus.CREATED).body("Email Verified successfully!");
    }


}
