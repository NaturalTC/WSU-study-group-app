package com.github.wsustudygroupapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configures Spring Security for the WSU Study Group App.
 *
 * Key decisions:
 * - CSRF disabled: not needed for stateless JWT REST APIs (no browser session cookies)
 * - Stateless sessions: the server never stores session state — the JWT token proves identity on every request
 * - Public routes: /auth/** (register, login, verify) and Swagger UI are accessible without a token
 * - All other routes require a valid JWT token in the Authorization header
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Defines the security rules applied to every incoming HTTP request.
     * The JwtAuthFilter (added in Sprint 1) will be registered here to validate tokens.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/api-docs/**").permitAll()
                .anyRequest().authenticated()
            );

        return http.build();
    }

    /**
     * Provides the password hashing algorithm used across the app.
     * BCrypt is the industry standard — it salts and hashes passwords so they can never be reversed.
     * AuthService uses this bean to hash passwords on registration and verify them on login.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
