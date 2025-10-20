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
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@AllArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private final AppUserService appUserService;


    @Bean
    public UserDetailsService userDetailService() {
        return  appUserService;
    }

    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
         provider.setUserDetailsService(appUserService);
         return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                //This line not for production leads to threat and exploit of security
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/", "/login", "/signup", "/req/signup", "/css/**", "/js/**", "/error").permitAll();
                    auth.anyRequest().authenticated();
                })
                .formLogin(form -> {
                    form.loginPage("/login")
                            .loginProcessingUrl("/perform_login")
                            .defaultSuccessUrl("/", true)
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
}
