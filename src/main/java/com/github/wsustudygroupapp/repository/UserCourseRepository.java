package com.github.wsustudygroupapp.repository;

import com.github.wsustudygroupapp.model.UserCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /** Returns all enrollments in a course+section, excluding the given profile. */
    @Query("SELECT uc FROM UserCourse uc WHERE uc.course.id = :courseId AND uc.section = :section AND uc.profile.id <> :excludedProfileId")
    List<UserCourse> findByCourseAndSectionExcluding(@Param("courseId") Long courseId,
                                                     @Param("section") String section,
                                                     @Param("excludedProfileId") Long excludedProfileId);

    /** Returns all enrollments in a course, excluding the given profile. */
    @Query("SELECT uc FROM UserCourse uc WHERE uc.course.id = :courseId AND uc.profile.id <> :excludedProfileId")
    List<UserCourse> findAllByCourseExcluding(@Param("courseId") Long courseId,
                                              @Param("excludedProfileId") Long excludedProfileId);
}
