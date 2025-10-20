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


//mark the class as a rest api controller that returns json responses also we create endpoints that receive and return json data
@RestController
public class RegistrationController {
//first we make dependency injection te model repository to handle database operations and the password encoder to encode the password and do the
    //hashing for us before it is saved in the database
    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private  PasswordEncoder passwordEncoder;

    /*now we make the url path  and specify that the endpoint will receive json data and return json data
    * after that we convert the json data to an appuser object thanks to request body  then thanks to the injections we hash the password and save it in db*/
    @PostMapping(value="/req/signup",consumes = "application/json",produces = "application/json")
    public AppUser createUser(@RequestBody AppUser appUser){
        appUser.setPassword(passwordEncoder.encode(appUser.getPassword()));
        return appUserRepository.save(appUser);
    }
}
