package com.example.demo.Controller;

import com.example.demo.User.BusinessUser;
import com.example.demo.User.BusinessUserRepository;
import com.example.demo.Service.EmailService;
import com.example.demo.utils.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
public class BusinessRegistrationController {

    @Autowired private BusinessUserRepository businessUserRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private EmailService emailService;

    @PostMapping(value = "/req/business/signup", consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> createBusiness(@RequestBody BusinessUser businessUser) {

        BusinessUser existing = businessUserRepository.findByEmail(businessUser.getEmail());
        if (existing != null) {
            if (existing.isVerified()) {
                return new ResponseEntity<>("Business already exists", null);
            } else {
                String verificationToken = JwtTokenUtil.generateToken(existing.getEmail());
                existing.setVerificationToken(verificationToken);
                businessUserRepository.save(existing);

                // IMPORTANT: use a BUSINESS verify endpoint
                emailService.sendBusinessVerificationEmail(existing.getEmail(), verificationToken);

                return new ResponseEntity<>("Created", HttpStatus.OK);
            }
        }

        businessUser.setPassword(passwordEncoder.encode(businessUser.getPassword()));
        String verificationToken = JwtTokenUtil.generateToken(businessUser.getEmail());
        businessUser.setVerificationToken(verificationToken);

        businessUser.setVerified(false);
        businessUser.setApproved(false);

        businessUserRepository.save(businessUser);

        emailService.sendBusinessVerificationEmail(businessUser.getEmail(), verificationToken);

        return new ResponseEntity<>("Created", HttpStatus.OK);
    }
}
