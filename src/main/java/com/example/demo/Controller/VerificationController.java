package com.example.demo.Controller;


import com.example.demo.User.AppUser;
import com.example.demo.User.AppUserRepository;
import com.example.demo.User.AppUserService;
import com.example.demo.utils.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VerificationController {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @GetMapping("/req/signup/verify")
    public ResponseEntity verifyEmail(@RequestParam("token")String token){

        String emailString=jwtTokenUtil.extractEmail(token);
        AppUser appuser = appUserRepository.findByEmail(emailString);
        if(appuser==null || appuser.getVerificationToken()==null ){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    "Token Expired or Invalid"
            );
        }
        if(!jwtTokenUtil.validateToken(token)||!appuser.getVerificationToken().equals(token)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    "Token Expired or Invalid"
            );
        }
        appuser.setVerificationToken(null);
        appuser.setVerified(true);

        appUserRepository.save(appuser);
        return ResponseEntity.status(HttpStatus.CREATED).body("Email Verified successfully!");


    }


}
