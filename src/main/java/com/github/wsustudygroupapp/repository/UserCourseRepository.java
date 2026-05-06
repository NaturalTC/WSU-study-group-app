package com.github.wsustudygroupapp.repository;

import com.github.wsustudygroupapp.model.UserCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Database operations for the UserCourse entity.
 */
@Repository
public interface UserCourseRepository extends JpaRepository<UserCourse, Long> {

    /** Returns all course enrollments for a given student profile. */
    List<UserCourse> findByProfileId(Long profileId);

    /** Returns all enrollments in a given course. Used by GamificationService for course leaderboard. */
    List<UserCourse> findByCourseId(Long courseId);

    /** Returns true if the student is already enrolled in this course. Used to prevent duplicate enrollments. */
    boolean existsByProfileIdAndCourseId(Long profileId, Long courseId);
}
