package com.github.wsustudygroupapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Represents a course from the WSU course catalog.
 * All courses are pre-seeded from data.sql on startup — students cannot add their own.
 * Students enroll in a course via {@link UserCourse}, which also stores their section and semester.
 */
@Entity
@Table(name = "COURSE_TABLE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    /** Auto-generated primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Department abbreviation (e.g. "CAIS", "MATH", "ENGL"). */
    @Column(nullable = false, length = 10)
    private String departmentCode;

    /** Full course identifier as it appears in the WSU catalog (e.g. "CAIS 0236"). Unique. */
    @Column(nullable = false, unique = true, length = 20)
    private String courseCode;

    /** Full course title (e.g. "Computer Organization and Architecture"). */
    @Column(nullable = false)
    private String courseName;

    /** All student enrollments in this course across all sections and semesters. */
    @OneToMany(mappedBy = "course")
    private List<UserCourse> enrollments;
}
