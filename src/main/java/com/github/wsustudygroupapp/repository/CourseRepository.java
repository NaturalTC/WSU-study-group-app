package com.github.wsustudygroupapp.repository;

import com.github.wsustudygroupapp.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Database operations for the Course entity.
 * All ~500 WSU courses are pre-seeded — these queries are read-only from the student's perspective.
 * Spring generates all SQL automatically from method names — no queries needed.
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    /** Returns all courses in a given department (e.g. all "CAIS" courses). */
    List<Course> findByDepartmentCode(String departmentCode);

    /** Finds a single course by its catalog code (e.g. "CAIS 0236"). Used during enrollment. */
    Optional<Course> findByCourseCode(String courseCode);

    /** Searches courses by name — case insensitive, partial match. Used for the course search bar. */
    List<Course> findByCourseNameContainingIgnoreCase(String keyword);
}
