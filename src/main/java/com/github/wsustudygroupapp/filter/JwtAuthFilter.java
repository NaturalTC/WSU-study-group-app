package com.github.wsustudygroupapp.filter;

import com.github.wsustudygroupapp.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// TODO: Jose — JWT authentication filter
// This runs on EVERY request before it reaches a controller
// It reads the Authorization header, validates the token, and tells Spring Security who the user is
// If the token is missing or invalid, the request is rejected here

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // TODO: Step 1 — read the Authorization header
        //  String authHeader = request.getHeader("Authorization");

        // TODO: Step 2 — check if the header exists and starts with "Bearer "
        //  if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        //      filterChain.doFilter(request, response); return;
        //  }

        // TODO: Step 3 — extract the token (everything after "Bearer ")
        //  String token = authHeader.substring(7);

        // TODO: Step 4 — extract the email from the token using JwtUtil
        //  String email = jwtUtil.extractEmail(token);

        // TODO: Step 5 — if email is valid and no auth is set yet, load the user
        //  UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        // TODO: Step 6 — validate the token
        //  if (jwtUtil.isTokenValid(token)) { ... }

        // TODO: Step 7 — set the authentication in Spring Security's context
        //  UsernamePasswordAuthenticationToken authToken =
        //      new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        //  SecurityContextHolder.getContext().setAuthentication(authToken);

        // TODO: Step 8 — continue the filter chain
        filterChain.doFilter(request, response);
    }
}
