package com.example.demo.Security;

import com.example.demo.User.AppUserService;
import com.example.demo.Service.BusinessUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AppUserService appUserService;
    private final BusinessUserService businessUserService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private AuthenticationProvider appProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(appUserService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    private AuthenticationProvider businessProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(businessUserService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain businessChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/business/**", "/req/business/**")
                .csrf(csrf -> csrf.disable())
                .authenticationProvider(businessProvider())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/business/login",
                                "/business/signup",
                                "/business/perform_login",
                                "/req/business/**",
                                "/css/**", "/js/**", "/images/**", "/videos/**", "/uploads/**",
                                "/error"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/business/login")
                        .loginProcessingUrl("/business/perform_login")
                        .defaultSuccessUrl("/business/dashboard", true)
                        .failureUrl("/business/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/business/logout")
                        .logoutSuccessUrl("/business/login?logout=true")
                        .permitAll()
                )
                .build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain appChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/**") // keep explicit
                .csrf(csrf -> csrf.disable())
                .authenticationProvider(appProvider())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/index",
                                "/login", "/signup", "/perform_login", "/req/**",
                                "/booking-success",
                                "/flights/checkout-success",
                                "/flights/checkout-cancel",
                                "/css/**", "/js/**", "/images/**", "/videos/**", "/uploads/**",
                                "/companies/**",
                                "/api/flights/**",
                                "/ws/**",
                                "/error"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/perform_login")
                        .defaultSuccessUrl("/index", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/perform_logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll()
                )
                .build();
    }
}
