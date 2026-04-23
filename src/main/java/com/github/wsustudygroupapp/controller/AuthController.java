package com.github.wsustudygroupapp.controller;

import com.github.wsustudygroupapp.dto.*;
import java.util.Map;
import com.github.wsustudygroupapp.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Jose — exposes auth endpoints
// Public routes are configured in SecurityConfig

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public AuthController(AuthService authService)
    {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request)
    {
        authService.register(request);
        return ResponseEntity.ok("Registration successful. Check your email to verify your account.");
    }

    @GetMapping("/verify")
    public ResponseEntity<Void> verify(@RequestParam String token)
    {
        try {
            authService.verify(token);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Location", frontendUrl + "/verify-success");
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        } catch (RuntimeException e) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Location", frontendUrl + "/verify-error");
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request)
    {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerification(@RequestBody Map<String, String> body) {
        authService.resendVerification(body.get("email"));
        return ResponseEntity.ok("Verification email resent. Check your inbox.");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request)
    {
        authService.forgotPassword(request);
        return ResponseEntity.ok("Password reset sent to email!");
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest request)
    {
        authService.changePassword(request);
        return ResponseEntity.ok("Password has been successfully changed");
    }

    @PostMapping("/update-password")
    public ResponseEntity<String> updatePassword(@RequestBody UpdatePasswordRequest request)
    {
        authService.updatePassword(request);
        return ResponseEntity.ok("Password updated successfully");
    }
}
