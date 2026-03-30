package com.github.wsustudygroupapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/*
    Represents a WSU course from the course catalog.
    Pre-seeded from data.sql — students cannot add their own courses.
    Linked to profiles via UserCourse (which also stores section + semester).
*/

@Entity
@Table(name = "COURSE_TABLE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // e.g. CAIS, MATH, ENGL
    @Column(nullable = false, length = 10)
    private String departmentCode;

    // e.g. CAIS 0236
    @Column(nullable = false, unique = true, length = 20)
    private String courseCode;

    // e.g. Computer Organization and Architecture
    @Column(nullable = false)
    private String courseName;

    @OneToMany(mappedBy = "course")
    private List<UserCourse> enrollments;
}
