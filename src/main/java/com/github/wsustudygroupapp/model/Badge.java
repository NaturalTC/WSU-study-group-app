package com.github.wsustudygroupapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Defines a badge that students can earn through participation.
 * Examples: "First Group Join", "10 Messages Sent", "Study Streak: 7 Days".
 * Badges are pre-seeded in the database — they are not created at runtime.
 */
@Entity
@Table(name = "badge_table")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Badge {

    /** Auto-generated primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Short display name shown on the student's profile (e.g. "Group Starter"). */
    @Column(nullable = false, unique = true)
    private String name;

    /** Longer description of what the student did to earn this badge. */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** Emoji or icon identifier used by the frontend to render the badge visually. */
    @Column
    private String icon;

    /** How many points this badge awards when earned. */
    @Column(nullable = false)
    private int pointValue;

    // TODO: Maicheal Shenouda — add a BadgeType enum (MILESTONE, STREAK, SOCIAL, etc.)
    //       and store it here so the frontend can group badges by category
}
