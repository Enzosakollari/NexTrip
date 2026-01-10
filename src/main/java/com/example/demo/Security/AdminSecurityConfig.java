package com.example.demo.Security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class AdminSecurityConfig {

    private final AdminDetailsService adminDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Bean
    @Order(1)
    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/admin/**")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/login", "/admin/perform_login", "/admin/logout").permitAll()
                        .anyRequest().hasAuthority("ADMIN")
                )
                .authenticationProvider(adminAuthProvider())
                .formLogin(form -> form
                        .loginPage("/admin/login")
                        .loginProcessingUrl("/admin/perform_login")
                        .defaultSuccessUrl("/admin", true)
                        .failureUrl("/admin/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/admin/logout")
                        .logoutSuccessUrl("/admin/login?logout=true")
                )
                .csrf(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider adminAuthProvider() {
        var provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(adminDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }
}
