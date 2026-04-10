package com.github.wsustudygroupapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents an authenticated user account.
 * Stores credentials and verification state only — no app-specific data.
 * All student-facing data (name, major, bio) lives in {@link Profile}.
 */
@Entity
@Table(name = "USER_TABLE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /** Auto-generated primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Optional display username. */
    @Column
    private String username;

    /** School email — must end with @westfield.ma.edu. Unique across all users. */
    @Column(nullable = false, unique = true)
    private String email;

    /** BCrypt-hashed password. Never stored as plain text. */
    @Column(nullable = false)
    private String password;

    /** User role for access control — default is "USER", reserved for future "ADMIN" use. */
    @Column
    private String role;

    /** False until the student clicks the verification link sent to their school email. */
    @Column
    private boolean isVerified = false;

    /** UUID token emailed to the student on registration. Cleared after verification. */
    @Column
    private String verificationToken;

    /** UUID token emailed when a student requests a password reset. Cleared after use. */
    @Column
    private String resetToken;

    /** The student's profile — created automatically after email verification. */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Profile profile;
}
