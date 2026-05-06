package com.github.wsustudygroupapp.config;

import com.github.wsustudygroupapp.filter.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

// Jose — configures Spring Security for the entire app
// Plugs in our JwtAuthFilter so every request gets checked for a valid token
// Sets the app to stateless — no sessions, JWT proves identity on every request

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.frontend-url-extra:}")
    private String frontendUrlExtra;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter)
    {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    /*
        The security filter chain — defines the rules applied to every incoming HTTP request.

        CSRF disabled — not needed for stateless JWT REST APIs (no browser session cookies)
        Stateless sessions — the server never stores session state, the JWT proves identity on every request
        Public routes — /auth/** and Swagger are open, everything else requires a valid JWT
        JwtAuthFilter runs before Spring's own auth filter on every request
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception
    {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable) // no CSRF needed, we're stateless no cookies impossible for ( cross state reference
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // no sessions — JWT only
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/register", "/auth/verify", "/auth/login",
                        "/auth/forgot-password", "/auth/change-password",
                        "/auth/resend-verification").permitAll() // public auth routes
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/api-docs/**").permitAll() // Swagger UI open
                .requestMatchers("/ws/**").permitAll() // WebSocket upgrade — JWT passed as STOMP header instead
                .anyRequest().authenticated() // everything else needs a valid JWT
            )
            // Plug our filter in — runs before Spring's built-in auth filter on every request
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        List<String> origins = frontendUrlExtra.isBlank()
                ? List.of(frontendUrl)
                : List.of(frontendUrl, frontendUrlExtra);
        config.setAllowedOrigins(origins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // BCrypt password encoder — used by AuthService to hash passwords on register and verify them on login
    // Industry standard — salts and hashes so passwords can never be reversed even if DB is breached
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
