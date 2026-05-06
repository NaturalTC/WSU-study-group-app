package com.github.wsustudygroupapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Stores student-facing data for a registered user.
 * Kept separate from {@link User} so authentication logic stays isolated from app data.
 * One Profile exists per User — created when the student completes onboarding after verification.
 */
@Entity
@Table(name = "PROFILE_TABLE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Profile {

    /** Auto-generated primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Student's display name shown across the app. */
    @Column
    private String name;

    /** Student's declared major (e.g. "Computer Science"). */
    @Column
    private String major;

    /** Academic year — Freshman, Sophomore, Junior, or Senior. */
    @Column
    private String year;

    /** Optional short bio visible to other students in shared study groups. */
    @Column(columnDefinition = "TEXT")
    private String bio;

    /** The User account this profile belongs to. */
    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /** All courses this student is enrolled in, each with a specific section and semester. */
    @JsonIgnore
    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserCourse> enrollments;

    @Column
    private String profilePicURL;

    // ── Sprint 2 fields ───────────────────────────────────────────────

    /** Total gamification points earned by this student. Incremented by GamificationService. */
    @Column(nullable = false)
    private int points = 0;

    // TODO: Maicheal Shenouda — add a @OneToMany to UserBadge so profile.getBadges() works
}
