package com.github.wsustudygroupapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Represents a study group created by a student for a specific course.
 * Any student enrolled in the same course can join the group.
 * Each group has its own real-time chat powered by WebSockets.
 */
@Entity
@Table(name = "study_group_table")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudyGroup {

    /** Auto-generated primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Display name of the group (e.g. "CAIS 0236 Study Crew"). */
    @Column(nullable = false)
    private String name;

    /** The course this study group is for. */
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    /** The student who created the group. Automatically added as the first member. */
    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private Profile createdBy;

    /** All students currently in this study group. */
    @ManyToMany
    @JoinTable(
        name = "study_group_members",
        joinColumns = @JoinColumn(name = "group_id"),
        inverseJoinColumns = @JoinColumn(name = "profile_id")
    )
    private List<Profile> members;

    /** All chat messages sent in this group, ordered by time. */
    @OneToMany(mappedBy = "studyGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages;
}
