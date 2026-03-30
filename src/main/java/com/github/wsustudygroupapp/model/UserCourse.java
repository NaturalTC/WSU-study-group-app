package com.github.wsustudygroupapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
    Explicit join entity between Profile and Course.
    Stores enrollment-specific data: which section and semester
    a student is in for a given course.
    This is what the matching algorithm queries against.
*/

@Entity
@Table(name = "USER_COURSE_TABLE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCourse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    // e.g. 001, 002
    @Column(nullable = false, length = 10)
    private String section;

    // e.g. Fall 2026, Spring 2026
    @Column(nullable = false, length = 20)
    private String semester;
}
