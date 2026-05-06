package com.github.wsustudygroupapp.controller;

import com.github.wsustudygroupapp.dto.CourseEnrollRequest;
import com.github.wsustudygroupapp.model.Course;
import com.github.wsustudygroupapp.model.UserCourse;
import com.github.wsustudygroupapp.service.CourseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// DONE: Maicheal — exposes course enrollment endpoints
// All routes here require a valid JWT token

@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    // DONE: call courseService.getAllCourses()
    // DONE: return 200 with the full course list (used for the frontend dropdown)
    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    // DONE: extract the logged-in user's profile ID
    // DONE: call courseService.getMyCourses(profileId)
    // DONE: return 200 with the student's enrolled courses
    @GetMapping("/my")
    public ResponseEntity<List<UserCourse>> getMyCourses(Authentication authentication) {
        return ResponseEntity.ok(courseService.getMyCourses(authentication.getName()));
    }

    // DONE: extract the logged-in user's profile ID
    // DONE: call courseService.enroll(profileId, request)
    // DONE: return 201
    @PostMapping("/enroll")
    public ResponseEntity<UserCourse> enroll(Authentication authentication,
                                              @RequestBody CourseEnrollRequest request) {
        // 201 Created — a new enrollment record was added
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(courseService.enroll(authentication.getName(), request));
    }

    // DONE: extract the logged-in user's profile ID
    // DONE: call courseService.drop(userCourseId, profileId)
    // DONE: return 204 No Content
    @DeleteMapping("/{userCourseId}")
    public ResponseEntity<Void> drop(Authentication authentication,
                                      @PathVariable Long userCourseId) {
        courseService.drop(userCourseId, authentication.getName());
        // 204 No Content — successful deletion with no response body
        return ResponseEntity.noContent().build();
    }

    // Search courses by keyword: GET /courses/search?q=biology
    @GetMapping("/search")
    public ResponseEntity<List<Course>> searchCourses(@RequestParam String q) {
        return ResponseEntity.ok(courseService.searchCourses(q));
    }

    // Filter by department code: GET /courses/department/CAIS
    @GetMapping("/department/{code}")
    public ResponseEntity<List<Course>> getCoursesByDepartment(@PathVariable String code) {
        return ResponseEntity.ok(courseService.getCoursesByDepartment(code));
    }
}
