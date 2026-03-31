package com.github.wsustudygroupapp.repository;

import com.github.wsustudygroupapp.model.UserCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Database operations for the UserCourse entity.
 * Contains the core classmate matching query that powers the study group finder.
 */
@Repository
public interface UserCourseRepository extends JpaRepository<UserCourse, Long> {

    /** Returns all course enrollments for a given student profile. */
    List<UserCourse> findByProfileId(Long profileId);

    /**
     * Core matching query — finds all students enrolled in the same course, section, and semester.
     * Excludes the requesting student's own profile from the results.
     * Two students are considered classmates only if all three values match exactly.
     */
    @Query("""
        SELECT uc FROM UserCourse uc
        WHERE uc.course.id = :courseId
        AND uc.section = :section
        AND uc.semester = :semester
        AND uc.profile.id != :profileId
    """)
    List<UserCourse> findClassmates(
        @Param("courseId") Long courseId,
        @Param("section") String section,
        @Param("semester") String semester,
        @Param("profileId") Long profileId
    );
}
