package com.github.wsustudygroupapp.repository;

import com.github.wsustudygroupapp.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByDepartmentCode(String departmentCode);
    Optional<Course> findByCourseCode(String courseCode);
    List<Course> findByCourseNameContainingIgnoreCase(String keyword);
}
