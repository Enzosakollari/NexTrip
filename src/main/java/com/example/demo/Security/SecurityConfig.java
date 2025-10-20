package com.example.demo.Security;

import com.example.demo.User.AppUserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@AllArgsConstructor
@EnableWebSecurity
public class SecurityConfig {
//autowire or inject the user service
    @Autowired
    private final AppUserService appUserService;


    //here we need user detail or user data we call this methdo and this calls the appUserService who has a detailed secton for this
    @Bean
    public UserDetailsService userDetailService() {
        return  appUserService;
    }

    //we are using this authentication provider to authenticate the user
    @Bean
    public AuthenticationProvider authenticationProvider(){
/*we instanciate the authentication provider and set the user details service and the password encoder
* we tell it where to check for users which in this case be our service class that has the method findByUsername
* then we tell the provider to encode the password*/
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
         provider.setUserDetailsService(appUserService);
         provider.setPasswordEncoder(passwordEncoder());
         return provider;
    }

/*here */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                //This line not for production leads to threat and exploit of security
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/", "/login", "/signup", "/req/signup", "/css/**", "/js/**", "/static/Images/**", "/error").permitAll();
                    auth.anyRequest().authenticated();
                })
                .formLogin(form -> {
                    form.loginPage("/login")
                            .loginProcessingUrl("/perform_login")
                            .defaultSuccessUrl("/index", true)
                            .failureUrl("/login?error=true")
                            .permitAll();
                })
                .logout(logout -> {
                    logout.logoutUrl("/perform_logout")
                            .logoutSuccessUrl("/login?logout=true")
                            .permitAll();
                })
                .csrf(csrf -> csrf.disable())
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
