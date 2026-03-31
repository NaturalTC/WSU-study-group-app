package com.github.wsustudygroupapp.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

// TODO: Jose — JWT utility class
// Handles generating tokens on login and validating them on every request
// Used by: AuthService (generate), JwtAuthFilter (validate + extract)

@Component
public class JwtUtil {

    // TODO: pull secret and expiration from application-local.properties
    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration}")
    private long expirationMs;

    // TODO: build the signing key from the secret string
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // TODO: generate a JWT token — called after a successful login
    // the token's subject is the user's email
    public String generateToken(String email) {
        // TODO: implement using Jwts.builder()
        //  .setSubject(email)
        //  .setIssuedAt(new Date())
        //  .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
        //  .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        //  .compact()
        return null;
    }

    // TODO: extract the email from a token — called in JwtAuthFilter
    public String extractEmail(String token) {
        // TODO: return parseClaims(token).getSubject()
        return null;
    }

    // TODO: check if a token is valid and not expired
    public boolean isTokenValid(String token) {
        // TODO: try parseClaims(token), return true if no exception is thrown
        return false;
    }

    // TODO: parse the token and return its claims (subject, expiration, etc.)
    private Claims parseClaims(String token) {
        // TODO: implement using Jwts.parserBuilder()
        //  .setSigningKey(getSigningKey())
        //  .build()
        //  .parseClaimsJws(token)
        //  .getBody()
        return null;
    }
}
