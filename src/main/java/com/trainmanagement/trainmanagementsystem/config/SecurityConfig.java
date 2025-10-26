package com.trainmanagement.trainmanagementsystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                        // Static resources
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        // Public pages
                        .requestMatchers("/", "/login", "/register", "/logout").permitAll()
                        // Authentication endpoints
                        .requestMatchers("/login/passenger", "/register/passenger").permitAll()
                        // Train search and booking (public)
                        .requestMatchers("/trains/**", "/bookings/**").permitAll()
                        // Passenger dashboard and related pages (public for now)
                        .requestMatchers("/passenger/**").permitAll()
                        // All other requests require authentication
                        .anyRequest().authenticated());
        // Removed formLogin() to allow custom controller-based login
        return http.build();
    }
}
