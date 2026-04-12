package com.github.wsustudygroupapp.controller;

import com.github.wsustudygroupapp.dto.ProfileRequest;
import com.github.wsustudygroupapp.model.Profile;
import com.github.wsustudygroupapp.service.ProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

// DONE: Maicheal — exposes profile endpoints
// All routes here require a valid JWT token (configured in SecurityConfig)

@RestController
@RequestMapping("/profiles")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    // DONE: extract the logged-in user's ID from the JWT (Spring Security principal)
    // DONE: call profileService.getProfile(userId)
    // DONE: return 200 with the Profile
    @GetMapping
    public ResponseEntity<Profile> getMyProfile(Authentication authentication) {
        // getName() returns the email stored in the JWT by JwtAuthFilter
        return ResponseEntity.ok(profileService.getProfile(authentication.getName()));
    }

    // DONE: extract the logged-in user's ID from the JWT
    // DONE: call profileService.createProfile(userId, request)
    // DONE: return 201 with the created Profile
    @PostMapping
    public ResponseEntity<Profile> createProfile(Authentication authentication,
                                                  @RequestBody ProfileRequest request) {
        // 201 Created is more precise than 200 OK for resource creation
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(profileService.createProfile(authentication.getName(), request));
    }

    // DONE: extract the logged-in user's ID from the JWT
    // DONE: call profileService.updateProfile(userId, request)
    // DONE: return 200 with the updated Profile
    @PutMapping
    public ResponseEntity<Profile> updateProfile(Authentication authentication,
                                                  @RequestBody ProfileRequest request) {
        return ResponseEntity.ok(profileService.updateProfile(authentication.getName(), request));
    }
}
