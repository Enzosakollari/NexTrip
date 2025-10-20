package com.example.demo.User;

import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AppUserService implements UserDetailsService {

    @Autowired
    private AppUserRepository appUserRepository;

    /*Here we use UserDetails that is a spring security interface that has a users secret info*/
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //here we check if the user exists in the database
        Optional<AppUser> user = appUserRepository.findByUsername(username);
        if(user.isPresent()){
            var userObj = user.get();
            //now to get userdetails not individual parameters we bundle them all together into userdetails and return it
            //that is done through the builder method which is provided by spring security
            return User.builder()
                    .username(userObj.getUsername())
                    .password(userObj.getPassword())
                    .authorities(new ArrayList<>())
                    .build();

        }else{
        throw new UsernameNotFoundException("User not found with username: " + username);
    }
    }

}