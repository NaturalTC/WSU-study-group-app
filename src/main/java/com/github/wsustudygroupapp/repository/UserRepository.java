package com.github.wsustudygroupapp.repository;

import com.github.wsustudygroupapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Database operations for the User entity.
 * Spring generates all SQL automatically from method names — no queries needed.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /** Finds a user by their school email. Used during login and registration checks. */
    Optional<User> findByEmail(String email);

    /** Finds a user by their email verification token. Used to verify a new account. */
    Optional<User> findByVerificationToken(String token);

    /** Returns true if an account with this email already exists. Used to prevent duplicate registrations. */
    boolean existsByEmail(String email);

    /** Finds a user by their password reset token. Used to validate a reset link. */
    Optional<User> findByResetToken(String resetToken);
}
