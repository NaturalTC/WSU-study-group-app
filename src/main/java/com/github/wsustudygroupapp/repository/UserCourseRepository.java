package com.github.wsustudygroupapp.repository;

import com.github.wsustudygroupapp.model.UserCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCourseRepository extends JpaRepository<UserCourse, Long> {

    // Get all enrollments for a profile
    List<UserCourse> findByProfileId(Long profileId);

    // Find other students in the same course + section + semester (for matching)
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
