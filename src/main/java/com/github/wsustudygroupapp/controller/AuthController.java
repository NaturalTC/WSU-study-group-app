package com.github.wsustudygroupapp.controller;

import com.github.wsustudygroupapp.dto.AuthResponse;
import com.github.wsustudygroupapp.dto.LoginRequest;
import com.github.wsustudygroupapp.dto.RegisterRequest;
import com.github.wsustudygroupapp.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// TODO: Jose — exposes the three auth endpoints
// These are public routes — no JWT token required (configured in SecurityConfig)

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // TODO: call authService.register(request)
    // TODO: return 200 with message "Registration successful. Check your email to verify your account."
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        return null;
    }

    // TODO: call authService.verify(token)
    // TODO: return 200 with message "Email verified. You can now log in."
    @GetMapping("/verify")
    public ResponseEntity<String> verify(@RequestParam String token) {
        return null;
    }

    // TODO: call authService.login(request)
    // TODO: return 200 with the AuthResponse (contains the JWT token)
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return null;
    }
}
