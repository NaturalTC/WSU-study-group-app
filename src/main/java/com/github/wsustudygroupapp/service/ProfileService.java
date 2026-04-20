package com.github.wsustudygroupapp.service;

import com.github.wsustudygroupapp.dto.ProfileRequest;
import com.github.wsustudygroupapp.exception.ResourceNotFoundException;
import com.github.wsustudygroupapp.model.Profile;
import com.github.wsustudygroupapp.model.User;
import com.github.wsustudygroupapp.repository.ProfileRepository;
import com.github.wsustudygroupapp.repository.UserRepository;
import org.springframework.stereotype.Service;

// DONE: Maicheal — handles profile creation and updates
// getProfile() → return a student's profile by their user ID
// createProfile() → called after registration to set up name, major, year, bio
// updateProfile() → let the student edit their profile later

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    public ProfileService(ProfileRepository profileRepository, UserRepository userRepository) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    // DONE: find profile by userId using profileRepository.findByUserId(userId)
    // DONE: throw exception if not found
    public Profile getProfile(String email) {
        // look up the User by email — throws 404 if account doesn't exist
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        // look up the Profile linked to that user — throws 404 if onboarding was never completed
        return profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for: " + email));
    }

    // DONE: build a new Profile from the request fields (name, major, year, bio)
    // DONE: link the profile to the User with the given userId
    // DONE: save and return the profile
    public Profile createProfile(String email, ProfileRequest request) {
        // resolve the User account so we can link the profile to it
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        // build and populate the new profile from the request body
        Profile profile = new Profile();
        profile.setUser(user);
        profile.setName(request.getName());
        profile.setMajor(request.getMajor());
        profile.setYear(request.getYear());
        profile.setBio(request.getBio());
        return profileRepository.save(profile);
    }

    // DONE: find the existing profile by userId
    // DONE: update fields from the request
    // DONE: save and return the updated profile
    public Profile updateProfile(String email, ProfileRequest request) {
        // resolve the User and their existing profile — both must exist
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for: " + email));
        // overwrite all fields with the new values from the request
        profile.setName(request.getName());
        profile.setMajor(request.getMajor());
        profile.setYear(request.getYear());
        profile.setBio(request.getBio());
        return profileRepository.save(profile);
    }
}
