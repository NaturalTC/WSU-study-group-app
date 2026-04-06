package com.github.wsustudygroupapp.filter;

import com.github.wsustudygroupapp.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import
        org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import
        org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;

class JwtAuthFilterTest {

    private JwtUtil jwtUtil;
    private UserDetailsService userDetailsService;
    private UserDetails userDetails;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;
    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setup() {
        jwtUtil = Mockito.mock(JwtUtil.class); // generate, validate, parse token
        userDetailsService = Mockito.mock(UserDetailsService.class);
        userDetails = Mockito.mock(UserDetails.class);
        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.mock(HttpServletResponse.class);
        filterChain = Mockito.mock(FilterChain.class);
        jwtAuthFilter = new JwtAuthFilter(jwtUtil,
                userDetailsService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void noAuthHeader_passesThrough() throws ServletException, IOException {

        // Arrange — no Authorization header
        Mockito.when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Assert — filter chain was called, no auth set
        Mockito.verify(filterChain).doFilter(request, response);
        Assertions.assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void invalidBearerPrefix_passesThrough() {
        // TODO
    }

    @Test
    void validToken_setsAuthentication() {
        // TODO
    }

    @Test
    void invalidToken_doesNotSetAuthentication() {
        // TODO
    }
}