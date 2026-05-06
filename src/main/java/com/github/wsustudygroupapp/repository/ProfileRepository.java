package com.github.wsustudygroupapp.repository;

import com.github.wsustudygroupapp.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Database operations for the Profile entity.
 * Spring generates all SQL automatically from method names — no queries needed.
 */
@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    /** Finds a student's profile by their User ID. Used to load profile data after login. */
    Optional<Profile> findByUserId(Long userId);
    Optional<Profile> findByName(String name);
    Optional<Profile> findByUserEmail(String email);

    /** Case-insensitive partial name search — powers the friend search bar. */
    java.util.List<Profile> findByNameContainingIgnoreCase(String name);
}
