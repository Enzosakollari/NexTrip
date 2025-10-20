package com.example.demo.Controller;

import com.example.demo.User.AppUser;
import com.example.demo.User.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RegistrationController {

    @Autowired
    private AppUserRepository appUserRepository;

    @PostMapping(value="/req/signup",consumes = "application/json")
    public AppUser createUser(@RequestBody AppUser appUser){
        return appUserRepository.save(appUser);
    }
}
