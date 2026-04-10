package com.github.wsustudygroupapp.controller;

import com.github.wsustudygroupapp.dto.*;
import com.github.wsustudygroupapp.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Jose — exposes the three auth endpoints
// These are public routes — no JWT token required (configured in SecurityConfig)
// All three just delegate straight to AuthService, no logic lives here

@RestController
@RequestMapping("/auth")
public class AuthController {

    // AuthService handles all the actual logic — controller just wires HTTP to it
    private final AuthService authService;

    public AuthController(AuthService authService)
    {
        this.authService = authService;
    }

    /*
        POST /auth/register
        Body: { "email": "jose@westfield.ma.edu", "password": "..." }
        Saves the user and fires the verification email
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request)
    {
        authService.register(request);
        return ResponseEntity.ok("Registration successful. Check your email to verify your account.");
    }

    /*
        GET /auth/verify?token=abc-123-uuid
        The link emailed to the student — marks their account as verified
     */
    @GetMapping("/verify")
    public ResponseEntity<String> verify(@RequestParam String token)
    {
        authService.verify(token);
        return ResponseEntity.ok("Email verified. You can now log in.");
    }

    /*
        POST /auth/login
        Body: { "email": "jose@westfield.ma.edu", "password": "..." }
        Returns the JWT token — client stores this and sends it on every future request
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request)
    {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request)
    {
        authService.forgotPassword(request);
        return ResponseEntity.ok("Password reset sent to email!");
    }

    @PostMapping("/change-password")
    private ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest request)
    {
        authService.changePassword(request);
        return ResponseEntity.ok("Password has been successfully changed");
    }

    /*
        POST /auth/update-password
        Requires: Authorization: Bearer <JWT>
        Body: { "currentPassword": "...", "newPassword": "..." }
        For logged-in users who want to change their password without a reset flow
     */
    @PostMapping("/update-password")
    public ResponseEntity<String> updatePassword(@RequestBody UpdatePasswordRequest request)
    {
        authService.updatePassword(request);
        return ResponseEntity.ok("Password updated successfully");
    }
}
