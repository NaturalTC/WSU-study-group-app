package com.github.wsustudygroupapp.service;

import com.github.wsustudygroupapp.dto.CourseEnrollRequest;
import com.github.wsustudygroupapp.exception.ResourceNotFoundException;
import com.github.wsustudygroupapp.model.Course;
import com.github.wsustudygroupapp.model.Profile;
import com.github.wsustudygroupapp.model.UserCourse;
import com.github.wsustudygroupapp.repository.CourseRepository;
import com.github.wsustudygroupapp.repository.ProfileRepository;
import com.github.wsustudygroupapp.repository.UserCourseRepository;
import com.github.wsustudygroupapp.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
    private final UserRepository userRepository;

    public CourseService(CourseRepository courseRepository,
                         UserCourseRepository userCourseRepository,
                         ProfileRepository profileRepository,
                         UserRepository userRepository) {
        this.courseRepository = courseRepository;
        this.userCourseRepository = userCourseRepository;
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    // DONE: return courseRepository.findAll()
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    // DONE: return userCourseRepository.findByProfileId(profileId)
    public List<UserCourse> getMyCourses(String email) {
        // resolve profile from the logged-in user's email
        Profile profile = resolveProfile(email);
        return userCourseRepository.findByProfileId(profile.getId());
    }

    // DONE: find the course by courseCode using courseRepository.findByCourseCode()
    // DONE: find the profile by profileId
    // DONE: build a new UserCourse with the course, profile, section, semester
    // DONE: save and return it
    public UserCourse enroll(String email, CourseEnrollRequest request) {
        Profile profile = resolveProfile(email);
        Course course = courseRepository.findByCourseCode(request.getCourseCode())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + request.getCourseCode()));
        // prevent the same student from enrolling in the same course+section+semester twice
        if (userCourseRepository.existsByProfileIdAndCourseIdAndSectionAndSemester(
                profile.getId(), course.getId(), request.getSection(), request.getSemester())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already enrolled in this course section");
        }
        UserCourse enrollment = new UserCourse();
        enrollment.setProfile(profile);
        enrollment.setCourse(course);
        enrollment.setSection(request.getSection());
        enrollment.setSemester(request.getSemester());
        return userCourseRepository.save(enrollment);
    }

    // DONE: find the UserCourse by its ID
    // DONE: confirm it belongs to this profileId (security check)
    // DONE: delete it
    public void drop(Long userCourseId, String email) {
        Profile profile = resolveProfile(email);
        UserCourse enrollment = userCourseRepository.findById(userCourseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found: " + userCourseId));
        // ensure students can only drop their own enrollments, not someone else's
        if (!enrollment.getProfile().getId().equals(profile.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only drop your own enrollments");
        }
        userCourseRepository.delete(enrollment);
    }

    /** Returns all students enrolled in a course excluding the requester, optionally filtered by section and/or semester. */
    public List<UserCourse> getEnrolledStudents(Long courseId, String section, String semester, String email) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found: " + courseId);
        }
        Profile profile = resolveProfile(email);
        return userCourseRepository.findCourseEnrollments(
                courseId,
                profile.getId(),
                (section != null && section.isBlank()) ? null : section,
                (semester != null && semester.isBlank()) ? null : semester
        );
    }

    // Plan C — search courses by keyword (e.g. "biology"), used for the frontend search bar
    public List<Course> searchCourses(String keyword) {
        return courseRepository.findByCourseNameContainingIgnoreCase(keyword);
    }

    // Plan C — filter courses by department code (e.g. "CAIS"), used for the frontend dropdown
    public List<Course> getCoursesByDepartment(String departmentCode) {
        return courseRepository.findByDepartmentCode(departmentCode);
    }

    // resolves the logged-in user's Profile from their email — throws 404 if not found
    private Profile resolveProfile(String email) {
        Long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email))
                .getId();
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for: " + email));
    }
}
