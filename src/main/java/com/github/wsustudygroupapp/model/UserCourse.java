package com.github.wsustudygroupapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Join entity linking a student's Profile to a Course with enrollment-specific data.
 * Stores which section and semester the student is in — not just which course.
 * This is what the classmate matching algorithm queries against:
 * two students are classmates if they share the same courseId, section, and semester.
 */
@Entity
@Table(name = "USER_COURSE_TABLE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCourse {

    /** Auto-generated primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The student enrolled in this course. */
    @ManyToOne
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    /** The course the student is enrolled in. */
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    /** The specific section the student is in (e.g. "001", "002"). */
    @Column(nullable = false, length = 10)
    private String section;

    /** The semester of enrollment (e.g. "Fall 2026", "Spring 2026"). */
    @Column(nullable = false, length = 20)
    private String semester;
}
