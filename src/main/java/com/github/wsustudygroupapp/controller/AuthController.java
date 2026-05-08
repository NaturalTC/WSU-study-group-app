package com.github.wsustudygroupapp.controller;

import com.github.wsustudygroupapp.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import com.github.wsustudygroupapp.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Jose — exposes auth endpoints
// Public routes are configured in SecurityConfig

@Tag(name = "Auth", description = "Registration, email verification, login, and password management")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService)
    {
        this.authService = authService;
    }

    @Operation(summary = "Register a new student account")
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request)
    {
        authService.register(request);
        return ResponseEntity.ok("Registration successful. Check your email to verify your account.");
    }

    @Operation(summary = "Verify a student's email address via token")
    @PostMapping("/verify")
    public ResponseEntity<String> verify(@RequestParam String token)
    {
        authService.verify(token);
        return ResponseEntity.ok("Email verified successfully");
    }

    @Operation(summary = "Login and receive a JWT token")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request)
    {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Resend the email verification link")
    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerification(@RequestBody Map<String, String> body) {
        authService.resendVerification(body.get("email"));
        return ResponseEntity.ok("Verification email resent. Check your inbox.");
    }

    @Operation(summary = "Send a password reset email")
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request)
    {
        authService.forgotPassword(request);
        return ResponseEntity.ok("Password reset sent to email!");
    }

    @Operation(summary = "Set a new password using a reset token")
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest request)
    {
        authService.changePassword(request);
        return ResponseEntity.ok("Password has been successfully changed");
    }

    @Operation(summary = "Update password for a logged-in user who knows their current password")
    @PostMapping("/update-password")
    public ResponseEntity<String> updatePassword(@RequestBody UpdatePasswordRequest request)
    {
        authService.updatePassword(request);
        return ResponseEntity.ok("Password updated successfully");
    }
}
