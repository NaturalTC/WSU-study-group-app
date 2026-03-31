package com.github.wsustudygroupapp.service;

import com.github.wsustudygroupapp.dto.ProfileRequest;
import com.github.wsustudygroupapp.model.Profile;
import com.github.wsustudygroupapp.repository.ProfileRepository;
import org.springframework.stereotype.Service;

// TODO: Maicheal — handles profile creation and updates
// getProfile() → return a student's profile by their user ID
// createProfile() → called after registration to set up name, major, year, bio
// updateProfile() → let the student edit their profile later

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;

    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    // TODO: find profile by userId using profileRepository.findByUserId(userId)
    // TODO: throw exception if not found
    public Profile getProfile(Long userId) {
        return null;
    }

    // TODO: build a new Profile from the request fields (name, major, year, bio)
    // TODO: link the profile to the User with the given userId
    // TODO: save and return the profile
    public Profile createProfile(Long userId, ProfileRequest request) {
        return null;
    }

    // TODO: find the existing profile by userId
    // TODO: update fields from the request
    // TODO: save and return the updated profile
    public Profile updateProfile(Long userId, ProfileRequest request) {
        return null;
    }
}
