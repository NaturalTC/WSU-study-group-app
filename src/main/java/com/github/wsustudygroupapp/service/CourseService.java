package com.github.wsustudygroupapp.service;

import com.github.wsustudygroupapp.dto.CourseEnrollRequest;
import com.github.wsustudygroupapp.model.Course;
import com.github.wsustudygroupapp.model.UserCourse;
import com.github.wsustudygroupapp.repository.CourseRepository;
import com.github.wsustudygroupapp.repository.ProfileRepository;
import com.github.wsustudygroupapp.repository.UserCourseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

// TODO: Maicheal — handles course enrollment and classmate matching
// getAllCourses() → return the full WSU catalog (for the dropdown on the frontend)
// getMyCourses() → return all courses a student is enrolled in
// enroll() → add a student to a course with their section + semester
// drop() → remove a student from a course
// getClassmates() → find other students in the same course + section + semester

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserCourseRepository userCourseRepository;
    private final ProfileRepository profileRepository;

    public CourseService(CourseRepository courseRepository,
                         UserCourseRepository userCourseRepository,
                         ProfileRepository profileRepository) {
        this.courseRepository = courseRepository;
        this.userCourseRepository = userCourseRepository;
        this.profileRepository = profileRepository;
    }

    // TODO: return courseRepository.findAll()
    public List<Course> getAllCourses() {
        return null;
    }

    // TODO: return userCourseRepository.findByProfileId(profileId)
    public List<UserCourse> getMyCourses(Long profileId) {
        return null;
    }

    // TODO: find the course by courseCode using courseRepository.findByCourseCode()
    // TODO: find the profile by profileId
    // TODO: build a new UserCourse with the course, profile, section, semester
    // TODO: save and return it
    public UserCourse enroll(Long profileId, CourseEnrollRequest request) {
        return null;
    }

    // TODO: find the UserCourse by its ID
    // TODO: confirm it belongs to this profileId (security check)
    // TODO: delete it
    public void drop(Long userCourseId, Long profileId) {

    }

    // TODO: find the UserCourse by ID to get courseId, section, semester
    // TODO: call userCourseRepository.findClassmates(courseId, section, semester, profileId)
    // TODO: return the list
    public List<UserCourse> getClassmates(Long userCourseId, Long profileId) {
        return null;
    }
}
