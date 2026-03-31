package com.github.wsustudygroupapp.controller;

import com.github.wsustudygroupapp.dto.CourseEnrollRequest;
import com.github.wsustudygroupapp.model.Course;
import com.github.wsustudygroupapp.model.UserCourse;
import com.github.wsustudygroupapp.service.CourseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// TODO: Maicheal — exposes course enrollment endpoints
// All routes here require a valid JWT token

@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    // TODO: call courseService.getAllCourses()
    // TODO: return 200 with the full course list (used for the frontend dropdown)
    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses() {
        return null;
    }

    // TODO: extract the logged-in user's profile ID
    // TODO: call courseService.getMyCourses(profileId)
    // TODO: return 200 with the student's enrolled courses
    @GetMapping("/my")
    public ResponseEntity<List<UserCourse>> getMyCourses() {
        return null;
    }

    // TODO: extract the logged-in user's profile ID
    // TODO: call courseService.enroll(profileId, request)
    // TODO: return 201
    @PostMapping("/enroll")
    public ResponseEntity<UserCourse> enroll(@RequestBody CourseEnrollRequest request) {
        return null;
    }

    // TODO: extract the logged-in user's profile ID
    // TODO: call courseService.drop(userCourseId, profileId)
    // TODO: return 204 No Content
    @DeleteMapping("/{userCourseId}")
    public ResponseEntity<Void> drop(@PathVariable Long userCourseId) {
        return null;
    }

    // TODO: extract the logged-in user's profile ID
    // TODO: call courseService.getClassmates(userCourseId, profileId)
    // TODO: return 200 with the list of classmates
    @GetMapping("/{userCourseId}/classmates")
    public ResponseEntity<List<UserCourse>> getClassmates(@PathVariable Long userCourseId) {
        return null;
    }
}
