package com.example.demo.Security;

import com.example.demo.User.AppUserService;
import com.example.demo.Service.BusinessUserService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.*;
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

    // --- Providers ---
    @Bean
    public AuthenticationProvider appAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(appUserService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationProvider businessAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(businessUserService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // --- SECURITY CHAIN 1: BUSINESS ---
    @Bean
    @Order(1)
    public SecurityFilterChain businessChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/business/**", "/req/business/**")
                .csrf(csrf -> csrf.disable())
                .authenticationProvider(businessAuthenticationProvider())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/business/login",
                                "/business/signup",
                                "/req/business/**",
                                "/css/**", "/js/**", "/images/**", "/videos/**",
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

    // --- SECURITY CHAIN 2: NORMAL USERS ---
    @Bean
    @Order(2)
    public SecurityFilterChain appChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authenticationProvider(appAuthenticationProvider())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/login", "/signup", "/req/**",
                                "/css/**", "/js/**", "/images/**", "/videos/**",
                                "/api/flights/**",
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
