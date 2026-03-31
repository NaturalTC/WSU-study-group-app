package com.github.wsustudygroupapp.controller;

import com.github.wsustudygroupapp.dto.ProfileRequest;
import com.github.wsustudygroupapp.model.Profile;
import com.github.wsustudygroupapp.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// TODO: Maicheal — exposes profile endpoints
// All routes here require a valid JWT token (configured in SecurityConfig)

@RestController
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    // TODO: extract the logged-in user's ID from the JWT (Spring Security principal)
    // TODO: call profileService.getProfile(userId)
    // TODO: return 200 with the Profile
    @GetMapping
    public ResponseEntity<Profile> getMyProfile() {
        return null;
    }

    // TODO: extract the logged-in user's ID from the JWT
    // TODO: call profileService.createProfile(userId, request)
    // TODO: return 201 with the created Profile
    @PostMapping
    public ResponseEntity<Profile> createProfile(@RequestBody ProfileRequest request) {
        return null;
    }

    // TODO: extract the logged-in user's ID from the JWT
    // TODO: call profileService.updateProfile(userId, request)
    // TODO: return 200 with the updated Profile
    @PutMapping
    public ResponseEntity<Profile> updateProfile(@RequestBody ProfileRequest request) {
        return null;
    }
}
