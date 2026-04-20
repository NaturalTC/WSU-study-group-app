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

// Jose — JWT authentication filter
// Runs on EVERY request before it reaches a controller
// Reads the Authorization header, validates the token, and tells Spring Security who the user is

/*
    Every HTTP request that comes into our app passes through a Security Filter Chain before it
    hits a controller. Think of it like a bouncer — this filter checks the JWT token on every
    request and either stamps it "verified" or lets Spring Security reject it.

    We extend OncePerRequestFilter so Spring guarantees this runs exactly once per request,
    even if Spring internally forwards the request somewhere else.
 */

// Registers this as a Spring bean so it can be injected into SecurityConfig
@Component
public class JwtAuthFilter extends OncePerRequestFilter
{

    // final — once Spring injects these at startup they can never be swapped out.
    // This bean is a singleton shared across every request simultaneously so we don't want these changing.
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    // Spring reads this constructor at startup and automatically injects both beans.
    // JwtUtil handles token math. UserDetailsService hits the DB to load the user.
    // UserDetailsServiceImpl is the concrete class Spring actually injects here.
    public JwtAuthFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
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
            filterChain.doFilter(request, response); return;
        }

        // Step 3 — strip "Bearer " off the front (7 characters), leaving just the raw token string
        String token = authHeader.substring(7);

        // Step 4 — decode the token and pull out the email (the subject we baked in at login)
        String email = jwtUtil.extractEmail(token);

        // Step 5 — load the full user from the database using that email
        // We hit the DB here because the user could have been deleted since the token was issued.
        // If the user no longer exists, skip authentication — Spring Security will return 401.
        UserDetails userDetails;
        try {
            userDetails = userDetailsService.loadUserByUsername(email);
        } catch (Exception e) {
            filterChain.doFilter(request, response);
            return;
        }

        // Step 6 — verify the token is legitimate and not expired
        if (jwtUtil.isTokenValid(token))
        {
            // Step 7 — tell Spring Security this user is authenticated for this request
            // UsernamePasswordAuthenticationToken wraps the user as "verified"
            // null = no password needed, we already proved identity via JWT
            // getAuthorities() = their roles/permissions, required to mark this as fully authenticated
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            // SecurityContextHolder is Spring's per-thread storage for the current user
            // Setting this here means any controller downstream knows who made this request
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        // Step 8 — pass the request to the next filter in the chain, eventually reaching the controller
        filterChain.doFilter(request, response);
    }
}
