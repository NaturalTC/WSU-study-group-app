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

    // Inject the secret value from properties
    @Value("${app.jwt.secret}")
    private String secret;

    // Inject the expiration time from properties
    @Value("${app.jwt.expiration}")
    private long expirationMs;

    // Takes our secret String and encrypts it (private key)
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /*
        Ok so pretty much we need to build this object called a JSON web-token
        so our web-app can be stateless. For example, if every API request required
        a query to our DB of our user and password means our web app is stateful.
        The solution to this problem is simple. We just want to send a JSON web-token (String)
        so that after a user logs in can send it in a packet header <Authorization: Bearer eyJhbGci...>
        Luckily the jwtt-api got us covered with the tools we need.
     */

    // Okay, the front door to our application is created here,
    // We will return a String that resembles a JWT token
    public String generateToken(String email)
    {
        // Jwts class provides a Builder pattern static method that takes attributes as setters.
        // Builder pattern is used for multiple arguments/method chaining
        // Pretty much a constructor    email -> JWT Token   with some data
        return Jwts.builder()
          .setSubject(email) // user email
          .setIssuedAt(new Date()) // date issued
          .setExpiration(new Date(System.currentTimeMillis() + expirationMs)) // expiration date
          .signWith(getSigningKey(), SignatureAlgorithm.HS256) // encryption for the jwt string
          .compact(); // build the string and return it
    }
    /*
            Three parts of packet
            Header is the algorithm we are using to sign it (HS256)
            This is the data into a JSON payload format (aka the data)
        {
            "sub": "jose@westfield.ma.edu",
            "iat": 1743465600,
            "exp": 1743552000
        }
            Then the signature is the Hash of your Header Payload and Secret Key all going through a hashing
            function. That way people don't just decode the JWT token themselves and send it back as admin
            The token can easily be decoded, the signing key puts our unique encryption on it
     */

    // A getter method that returns the email of the JWT token
    public String extractEmail(String token) {
        // The parseClaims decodes the token (more specifics in method)
        // after the information can be return using getter methods.
        // getSubject() just returns whatever is in the subject of the Payload
        return parseClaims(token).getSubject();
    }

    // Simple method that returns a T/F based on if no exception thrown during parsing
    public boolean isTokenValid(String token) {
        try
        {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // A JWT token in String format looks like this iuhfahebfkaubhsdiufh.aosiudhfiasuhf.oasuidhfoiauhf
    // The dots separate the header payload and signature

    // A method the returns the Payload Data (AKA a Claims Object)
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
        .setSigningKey(getSigningKey()) // verifying the signature with our key -- throws signature exception if failed
        .build() // configures the Parser object that is used to parse Tokens
        .parseClaimsJws(token) // Splits header | payload | signature. decodes each part using Base64url.
                               // verifies the signature (Throw Sig Excep). Deserializes into a JWS<Claims> wrapper.
                                // Checks Exp (Throws ExpiredJWTException).
        .getBody(); // finally it is returns the Claims Map object (AKA the Payload)
    }
}
