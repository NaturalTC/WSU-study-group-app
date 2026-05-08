package com.github.wsustudygroupapp.filter;

import com.github.wsustudygroupapp.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// Jose — JWT authentication filter
// Runs on EVERY request before it reaches a controller
// Reads the Authorization header, validates the token, and tells Spring Security who the user is

/*
    Every HTTP request that comes into our app passes through a Security Filter Chain before it
    hits a controller. Think of it like a bouncer — this filter checks the JWT token on every
    request and either stamps it "verified" or lets Spring Security reject it.

    We extend OncePerRequestFilter so Spring guarantees this runs exactly once per request,
    even if Spring internally forwards the request somewhere else.

    Fix 3 — no DB hit on every request.
    Previously this filter called userDetailsService.loadUserByUsername() on every single request,
    causing a DB query just to confirm the user exists. Instead, we embed the role in the JWT
    at login time and read it back here. The token signature already proves it hasn't been tampered
    with, so we trust the claims inside it without needing a DB round-trip.
 */

// Registers this as a Spring bean so it can be injected into SecurityConfig
@Component
public class JwtAuthFilter extends OncePerRequestFilter
{

    // final — once Spring injects this at startup it can never be swapped out.
    // This bean is a singleton shared across every request simultaneously.
    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    // This is the one method OncePerRequestFilter requires us to implement.
    // Spring calls this on every incoming HTTP request automatically.
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Step 1 — read the Authorization header from the request
        // Every request that has a token will include: Authorization: Bearer eyJhbGci...
        String authHeader = request.getHeader("Authorization");

        // Step 2 — if there's no token just pass the request through
        // We don't reject it here — Spring Security will block it if the route is protected
        if (authHeader == null || !authHeader.startsWith("Bearer "))
        {
            filterChain.doFilter(request, response);
            return;
        }

        // Step 3 — strip "Bearer " off the front (7 characters), leaving just the raw token string
        String token = authHeader.substring(7);

        // Step 4 — decode the token and pull out the email and role
        // Both were baked into the token at login so no DB lookup needed
        // Wrap in try-catch: malformed or expired tokens throw JwtException — skip authentication
        // and let Spring Security reject the request normally rather than blowing up with a 500.
        String email;
        String role;
        try {
            email = jwtUtil.extractEmail(token);
            role  = jwtUtil.extractRole(token);
        } catch (Exception e) {
            filterChain.doFilter(request, response);
            return;
        }

        // Step 5 — verify the token is legitimate and not expired, then authenticate
        if (jwtUtil.isTokenValid(token))
        {
            // "ROLE_USER" or "ROLE_ADMIN" — Spring Security's required prefix for role-based access
            List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

            // UsernamePasswordAuthenticationToken wraps the user as "verified"
            // email = principal (who they are), null = no password needed (JWT already proved it),
            // authorities = their roles so @PreAuthorize and route guards work correctly
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(email, null, authorities);

            // SecurityContextHolder is Spring's per-thread storage for the current user
            // Setting this here means any controller downstream knows who made this request
            // e.g. SecurityContextHolder.getContext().getAuthentication().getName() returns the email
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        // Step 6 — pass the request to the next filter in the chain, eventually reaching the controller
        filterChain.doFilter(request, response);
    }
}
