package com.github.wsustudygroupapp.controller;

import com.github.wsustudygroupapp.dto.CourseStudentResponse;
import com.github.wsustudygroupapp.model.Course;
import com.github.wsustudygroupapp.service.CourseService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    // GET /courses/{courseId}/students?section=001 (section param optional)
    @GetMapping("/{courseId}/students")
    public ResponseEntity<List<CourseStudentResponse>> getCourseStudents(
            Authentication authentication,
            @PathVariable Long courseId,
            @RequestParam(required = false) String section) {
        return ResponseEntity.ok(courseService.getCourseStudents(courseId, section, authentication.getName()));
    }
}
