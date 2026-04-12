package com.github.wsustudygroupapp.service;

import com.github.wsustudygroupapp.dto.ProfileRequest;
import com.github.wsustudygroupapp.exception.ResourceNotFoundException;
import com.github.wsustudygroupapp.model.Profile;
import com.github.wsustudygroupapp.model.User;
import com.github.wsustudygroupapp.repository.ProfileRepository;
import com.github.wsustudygroupapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock private ProfileRepository profileRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private ProfileService profileService;

    private static final String EMAIL = "test@westfield.ma.edu";
    private static final String UNKNOWN_EMAIL = "ghost@westfield.ma.edu";

    private User mockUser;
    private Profile mockProfile;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail(EMAIL);

        mockProfile = new Profile();
        mockProfile.setId(1L);
        mockProfile.setUser(mockUser);
        mockProfile.setName("Test User");
        mockProfile.setMajor("Computer Science");
        mockProfile.setYear("Junior");
        mockProfile.setBio("Test bio");
    }

    // ── getProfile ────────────────────────────────────────────────────────────

    @Test
    void getProfile_userNotFound_throwsResourceNotFoundWithEmailInMessage() {
        when(userRepository.findByEmail(UNKNOWN_EMAIL)).thenReturn(Optional.empty());
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> profileService.getProfile(UNKNOWN_EMAIL));
        // error message should identify which email failed
        assertTrue(ex.getMessage().contains(UNKNOWN_EMAIL));
    }

    @Test
    void getProfile_userExistsButNoProfile_throwsResourceNotFoundException() {
        // user account exists but onboarding was never completed
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> profileService.getProfile(EMAIL));
    }

    @Test
    void getProfile_bothExist_returnsCorrectProfile() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(mockProfile));
        assertEquals(mockProfile, profileService.getProfile(EMAIL));
    }

    // ── createProfile ─────────────────────────────────────────────────────────

    @Test
    void createProfile_userNotFound_throwsResourceNotFoundException() {
        when(userRepository.findByEmail(UNKNOWN_EMAIL)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> profileService.createProfile(UNKNOWN_EMAIL, new ProfileRequest()));
    }

    @Test
    void createProfile_setsAllRequestFieldsOnNewProfile() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));
        // return the profile as-is so we can assert on its fields
        when(profileRepository.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));

        ProfileRequest request = new ProfileRequest();
        request.setName("Jane Doe");
        request.setMajor("Biology");
        request.setYear("Sophomore");
        request.setBio("Loves science");

        Profile result = profileService.createProfile(EMAIL, request);

        assertEquals("Jane Doe", result.getName());
        assertEquals("Biology", result.getMajor());
        assertEquals("Sophomore", result.getYear());
        assertEquals("Loves science", result.getBio());
        assertEquals(mockUser, result.getUser());
    }

    @Test
    void createProfile_linksProfileToCorrectUser() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));
        when(profileRepository.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));

        Profile result = profileService.createProfile(EMAIL, new ProfileRequest());

        // the profile must be linked to the user resolved from the email
        assertEquals(mockUser, result.getUser());
    }

    @Test
    void createProfile_persistsProfileExactlyOnce() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));
        when(profileRepository.save(any(Profile.class))).thenReturn(mockProfile);

        profileService.createProfile(EMAIL, new ProfileRequest());

        verify(profileRepository, times(1)).save(any(Profile.class));
    }

    // ── updateProfile ─────────────────────────────────────────────────────────

    @Test
    void updateProfile_userNotFound_throwsResourceNotFoundException() {
        when(userRepository.findByEmail(UNKNOWN_EMAIL)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> profileService.updateProfile(UNKNOWN_EMAIL, new ProfileRequest()));
    }

    @Test
    void updateProfile_profileNotFound_throwsResourceNotFoundException() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> profileService.updateProfile(EMAIL, new ProfileRequest()));
    }

    @Test
    void updateProfile_overwritesAllFourFields() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(mockProfile));
        when(profileRepository.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));

        ProfileRequest request = new ProfileRequest();
        request.setName("New Name");
        request.setMajor("Physics");
        request.setYear("Senior");
        request.setBio("New bio");

        Profile result = profileService.updateProfile(EMAIL, request);

        assertEquals("New Name", result.getName());
        assertEquals("Physics", result.getMajor());
        assertEquals("Senior", result.getYear());
        assertEquals("New bio", result.getBio());
    }

    @Test
    void updateProfile_savesTheExistingProfileObject_notANewOne() {
        // must update and save the existing profile, not create a new one
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(mockProfile));
        when(profileRepository.save(mockProfile)).thenReturn(mockProfile);

        profileService.updateProfile(EMAIL, new ProfileRequest());

        // verify save is called with the exact existing profile instance
        verify(profileRepository, times(1)).save(mockProfile);
    }
}
