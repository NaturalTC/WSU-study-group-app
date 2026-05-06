package com.github.wsustudygroupapp.controller;

import com.github.wsustudygroupapp.dto.ProfileRequest;
import com.github.wsustudygroupapp.exception.ResourceNotFoundException;
import com.github.wsustudygroupapp.model.Profile;
import com.github.wsustudygroupapp.service.ProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {

    @Mock private ProfileService profileService;
    @Mock private Authentication authentication;
    @InjectMocks private ProfileController profileController;

    private static final String EMAIL = "test@westfield.ma.edu";
    private Profile mockProfile;
    private ProfileRequest profileRequest;

    @BeforeEach
    void setUp() {
        // lenient — not every test uses authentication (avoids UnnecessaryStubbingException)
        lenient().when(authentication.getName()).thenReturn(EMAIL);

        mockProfile = new Profile();
        mockProfile.setId(1L);
        mockProfile.setName("Test User");
        mockProfile.setMajor("Computer Science");
        mockProfile.setYear("Junior");
        mockProfile.setBio("Test bio");

        profileRequest = new ProfileRequest();
        profileRequest.setName("Test User");
        profileRequest.setMajor("Computer Science");
        profileRequest.setYear("Junior");
        profileRequest.setBio("Test bio");
    }

    // ── GET /profile ──────────────────────────────────────────────────────────

    @Test
    void getMyProfile_returns200WithProfile() {
        when(profileService.getProfile(EMAIL)).thenReturn(mockProfile);
        ResponseEntity<Profile> response = profileController.getMyProfile(authentication);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockProfile, response.getBody());
    }

    @Test
    void getMyProfile_passesEmailFromPrincipalToService() {
        when(profileService.getProfile(EMAIL)).thenReturn(mockProfile);
        profileController.getMyProfile(authentication);
        verify(profileService, times(1)).getProfile(EMAIL);
    }

    @Test
    void getMyProfile_profileNotFound_propagatesResourceNotFoundException() {
        // exception propagates up — Spring MVC maps @ResponseStatus(404) to HTTP 404
        when(profileService.getProfile(EMAIL))
                .thenThrow(new ResourceNotFoundException("Profile not found"));
        assertThrows(ResourceNotFoundException.class,
                () -> profileController.getMyProfile(authentication));
    }

    @Test
    void getMyProfile_userNotFound_propagatesResourceNotFoundException() {
        when(profileService.getProfile(EMAIL))
                .thenThrow(new ResourceNotFoundException("User not found: " + EMAIL));
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> profileController.getMyProfile(authentication));
        assertTrue(ex.getMessage().contains(EMAIL));
    }

    // ── POST /profile ─────────────────────────────────────────────────────────

    @Test
    void createProfile_returns201WithCreatedProfile() {
        when(profileService.createProfile(eq(EMAIL), any(ProfileRequest.class))).thenReturn(mockProfile);
        ResponseEntity<Profile> response = profileController.createProfile(authentication, profileRequest);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(mockProfile, response.getBody());
    }

    @Test
    void createProfile_passesEmailAndRequestBodyToService() {
        when(profileService.createProfile(eq(EMAIL), eq(profileRequest))).thenReturn(mockProfile);
        profileController.createProfile(authentication, profileRequest);
        verify(profileService, times(1)).createProfile(EMAIL, profileRequest);
    }

    @Test
    void createProfile_userNotFound_propagatesResourceNotFoundException() {
        when(profileService.createProfile(eq(EMAIL), any(ProfileRequest.class)))
                .thenThrow(new ResourceNotFoundException("User not found"));
        assertThrows(ResourceNotFoundException.class,
                () -> profileController.createProfile(authentication, profileRequest));
    }

    @Test
    void createProfile_doesNotReturn200_mustBe201() {
        // creating a resource must return 201, not 200
        when(profileService.createProfile(eq(EMAIL), any(ProfileRequest.class))).thenReturn(mockProfile);
        ResponseEntity<Profile> response = profileController.createProfile(authentication, profileRequest);
        assertNotEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    // ── PUT /profile ──────────────────────────────────────────────────────────

    @Test
    void updateProfile_returns200WithUpdatedProfile() {
        when(profileService.updateProfile(eq(EMAIL), any(ProfileRequest.class))).thenReturn(mockProfile);
        ResponseEntity<Profile> response = profileController.updateProfile(authentication, profileRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockProfile, response.getBody());
    }

    @Test
    void updateProfile_passesEmailAndRequestBodyToService() {
        when(profileService.updateProfile(eq(EMAIL), eq(profileRequest))).thenReturn(mockProfile);
        profileController.updateProfile(authentication, profileRequest);
        verify(profileService, times(1)).updateProfile(EMAIL, profileRequest);
    }

    @Test
    void updateProfile_profileNotFound_propagatesResourceNotFoundException() {
        when(profileService.updateProfile(eq(EMAIL), any(ProfileRequest.class)))
                .thenThrow(new ResourceNotFoundException("Profile not found"));
        assertThrows(ResourceNotFoundException.class,
                () -> profileController.updateProfile(authentication, profileRequest));
    }

    @Test
    void updateProfile_userNotFound_propagatesResourceNotFoundException() {
        when(profileService.updateProfile(eq(EMAIL), any(ProfileRequest.class)))
                .thenThrow(new ResourceNotFoundException("User not found: " + EMAIL));
        assertThrows(ResourceNotFoundException.class,
                () -> profileController.updateProfile(authentication, profileRequest));
    }
}
