package com.github.wsustudygroupapp.controller;

import com.github.wsustudygroupapp.dto.CourseStudentResponse;
import com.github.wsustudygroupapp.model.Course;
import com.github.wsustudygroupapp.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Courses", description = "Course catalog and classmate lookup")
@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @Operation(summary = "Get all courses in the WSU catalog")
    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    // GET /courses/{courseId}/students?section=001 (section param optional)
    @Operation(summary = "Get students enrolled in a course, optionally filtered by section")
    @GetMapping("/{courseId}/students")
    public ResponseEntity<List<CourseStudentResponse>> getCourseStudents(
            Authentication authentication,
            @PathVariable Long courseId,
            @RequestParam(required = false) String section) {
        return ResponseEntity.ok(courseService.getCourseStudents(courseId, section, authentication.getName()));
    }
}
