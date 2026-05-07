package com.github.wsustudygroupapp.controller;

import com.github.wsustudygroupapp.dto.BadgeResponseDTO;
import com.github.wsustudygroupapp.dto.ProfileRequest;
import com.github.wsustudygroupapp.model.Profile;
import com.github.wsustudygroupapp.service.GamificationService;
import com.github.wsustudygroupapp.service.ProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

// DONE: Maicheal — exposes profile endpoints
// All routes here require a valid JWT token (configured in SecurityConfig)

@RestController
@RequestMapping("/profiles")
public class ProfileController {

    private final ProfileService profileService;
    private final GamificationService gamificationService;

    public ProfileController(ProfileService profileService, GamificationService gamificationService) {
        this.profileService = profileService;
        this.gamificationService = gamificationService;
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

    @GetMapping("/{id}")
    public ResponseEntity<Profile> getProfileById(@PathVariable Long id) {
        return ResponseEntity.ok(profileService.getProfileById(id));
    }

    @PostMapping("/picture")
    public ResponseEntity<Profile> uploadProfilePicture(Authentication authentication,
                                                         @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(profileService.uploadProfilePicture(authentication.getName(), file));
    }

    @GetMapping("/badges")
    public ResponseEntity<List<BadgeResponseDTO>> getMyBadges(Authentication authentication) {
        return ResponseEntity.ok(gamificationService.getUserBadges(authentication.getName()));
    }
}
