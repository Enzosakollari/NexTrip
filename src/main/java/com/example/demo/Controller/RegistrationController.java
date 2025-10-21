package com.example.demo.Controller;

import com.example.demo.Service.EmailService;
import com.example.demo.User.AppUser;
import com.example.demo.User.AppUserRepository;
import com.example.demo.utils.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    @Autowired
    private EmailService emailService;

    /*now we make the url path  and specify that the endpoint will receive json data and return json data
    * after that we convert the json data to an appuser object thanks to request body  then thanks to the injections we hash the password and save it in db*/
    //switching to response entity to return a response entity object that contains the status code and the body of the response
    @PostMapping(value="/req/signup",consumes = "application/json",produces = "application/json")
    public  ResponseEntity<String> createUser(@RequestBody AppUser appUser){
        AppUser existingAppUser=appUserRepository.findByEmail(appUser.getEmail());
        if(existingAppUser!=null){
            if(existingAppUser.isVerified()){
                 return new ResponseEntity<>("User already exists",null);
             }else{
                String verificationToken= JwtTokenUtil.generateToken(appUser.getEmail());
                existingAppUser.setVerificationToken(verificationToken);
                appUserRepository.save(existingAppUser);
                //send email code
                emailService.sendVerificationEmail(existingAppUser.getEmail(),verificationToken);
                return new ResponseEntity<>("Created", HttpStatus.OK); // 200

            }
        }

        appUser.setPassword(passwordEncoder.encode(appUser.getPassword()));
        String verificationToken=JwtTokenUtil.generateToken(appUser.getEmail());
        appUser.setVerificationToken(verificationToken);
        appUserRepository.save(appUser);
        //send email code
        emailService.sendVerificationEmail(appUser.getEmail(),verificationToken);

        return new ResponseEntity<>("Created", HttpStatus.OK); // 200
    }
}
