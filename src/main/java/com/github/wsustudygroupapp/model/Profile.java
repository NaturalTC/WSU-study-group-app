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
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /** All courses this student is enrolled in, each with a specific section and semester. */
    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserCourse> enrollments;

    /**
     * Study groups this student created.
     * Cascade ALL + orphanRemoval ensures groups (and their messages) are deleted with the profile.
     */
    @JsonIgnore
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudyGroup> createdGroups;

    /**
     * Messages this student sent across all groups.
     * Cascade ALL + orphanRemoval deletes their messages when the profile is deleted.
     */
    @JsonIgnore
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> sentMessages;

    /**
     * Groups this student is a member of (inverse side of StudyGroup.members ManyToMany).
     * Not persisted here — StudyGroup owns the join table.
     * Used only by @PreRemove to clean up the study_group_members join table before deletion.
     */
    @JsonIgnore
    @ManyToMany(mappedBy = "members")
    private List<StudyGroup> memberOfGroups;

    /**
     * Before this profile is deleted, remove it from all group member lists.
     * This clears the study_group_members join table rows on the profile side —
     * JPA doesn't do this automatically since StudyGroup owns the join table.
     */
    @PreRemove
    private void removeFromGroups() {
        if (memberOfGroups != null) {
            for (StudyGroup group : memberOfGroups) {
                group.getMembers().remove(this);
            }
        }
    }
}
