package com.github.wsustudygroupapp.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setup() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "testsecretkey1234567890123456789012345");
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", 86400000L);
    }

    @Test
    void generateToken_returnsNonNullToken() {
        String token = jwtUtil.generateToken("jose@westfield.ma.edu");
        Assertions.assertNotNull(token);
    }

    @Test
    void extractEmail_returnsCorrectEmail() {
        String token = jwtUtil.generateToken("jose@westfield.ma.edu");
        Assertions.assertEquals("jose@westfield.ma.edu", jwtUtil.extractEmail(token));
    }

    @Test
    void isTokenValid_validToken_returnsTrue() {
        String token = jwtUtil.generateToken("jose@westfield.ma.edu");
        Assertions.assertTrue(jwtUtil.isTokenValid(token));
    }

    @Test
    void isTokenValid_garbageToken_returnsFalse() {
        Assertions.assertFalse(jwtUtil.isTokenValid("this.is.garbage"));
    }

    @Test
    void isTokenValid_expiredToken_returnsFalse() {
        // Set expiration to -1000ms so the token is already expired the moment it's created
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", -1000L);
        String token = jwtUtil.generateToken("jose@westfield.ma.edu");
        Assertions.assertFalse(jwtUtil.isTokenValid(token));
    }
}