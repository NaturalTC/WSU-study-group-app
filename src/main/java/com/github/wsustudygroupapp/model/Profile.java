package com.github.wsustudygroupapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private String major;

    @Column
    private String year;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column
    private String profilePicURL;

    @Column(nullable = false, columnDefinition = "INT NOT NULL DEFAULT 0")
    private int points = 0;

    // ── Cascade-delete collections ────────────────────────────────────
    // These are needed so deleting a User (→ Profile) cleans up all child rows
    // without foreign key violations. None of these collections are exposed by
    // any API — they exist purely to tell JPA the deletion order.

    @JsonIgnore
    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserCourse> courses = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserBadge> badges = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notification> notifications = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Friendship> sentFriendships = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Friendship> receivedFriendships = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "scheduledBy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MeetingSession> scheduledSessions = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudyGroup> createdGroups = new ArrayList<>();

    // Groups this profile is a member of (but didn't necessarily create).
    // Not cascade-deleted — we just remove the membership row via @PreRemove.
    @JsonIgnore
    @ManyToMany(mappedBy = "members")
    private List<StudyGroup> memberGroups = new ArrayList<>();

    // Before this profile is deleted, remove it from every group's member list
    // so the study_group_members join table rows are cleaned up first.
    @PreRemove
    private void removeFromMemberGroups() {
        for (StudyGroup group : new ArrayList<>(memberGroups)) {
            group.getMembers().remove(this);
        }
    }
}
