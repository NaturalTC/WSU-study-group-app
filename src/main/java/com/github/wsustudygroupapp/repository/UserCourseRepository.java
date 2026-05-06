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

    /** Returns all enrollments in a given course. Used by GamificationService for course leaderboard. */
    List<UserCourse> findByCourseId(Long courseId);

    /** Returns true if the student is already enrolled in this course. Used to prevent duplicate enrollments. */
    boolean existsByProfileIdAndCourseId(Long profileId, Long courseId);

    /**
     * Core matching query — finds all students enrolled in the same course.
     * Excludes the requesting student's own profile from the results.
     */
    @Query("""
        SELECT uc FROM UserCourse uc
        WHERE uc.course.id = :courseId
        AND uc.profile.id != :profileId
    """)
    List<UserCourse> findClassmates(
        @Param("courseId") Long courseId,
        @Param("profileId") Long profileId
    );

    /**
     * Returns all enrollments for a course, excluding the requesting student.
     */
    @Query("""
        SELECT uc FROM UserCourse uc
        WHERE uc.course.id = :courseId
        AND uc.profile.id != :profileId
    """)
    List<UserCourse> findCourseEnrollments(
        @Param("courseId") Long courseId,
        @Param("profileId") Long profileId
    );
}
