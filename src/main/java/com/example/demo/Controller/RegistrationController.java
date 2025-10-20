package com.example.demo.Controller;

import com.example.demo.User.AppUser;
import com.example.demo.User.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RegistrationController {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private  PasswordEncoder passwordEncoder;

    @PostMapping(value="/req/signup",consumes = "application/json",produces = "application/json")
    public AppUser createUser(@RequestBody AppUser appUser){
        // Encode the password before saving
        appUser.setPassword(passwordEncoder.encode(appUser.getPassword()));
        return appUserRepository.save(appUser);
    }
}
