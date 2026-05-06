package com.github.wsustudygroupapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Join entity linking a student's Profile to a Course.
 * Two students are classmates if they share the same courseId.
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


}
