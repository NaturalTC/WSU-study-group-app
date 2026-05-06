package com.github.wsustudygroupapp.controller;

import com.github.wsustudygroupapp.dto.CourseEnrollRequest;
import com.github.wsustudygroupapp.exception.ResourceNotFoundException;
import com.github.wsustudygroupapp.model.Course;
import com.github.wsustudygroupapp.model.UserCourse;
import com.github.wsustudygroupapp.service.CourseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseControllerTest {

    @Mock private CourseService courseService;
    @Mock private Authentication authentication;
    @InjectMocks private CourseController courseController;

    private static final String EMAIL = "test@westfield.ma.edu";
    private Course mockCourse;
    private UserCourse mockEnrollment;
    private CourseEnrollRequest enrollRequest;

    @BeforeEach
    void setUp() {
        // authentication stub is applied per-test via mockAuth() — not here,
        // because some tests (getAllCourses, search, department) don't use authentication

        mockCourse = new Course();
        mockCourse.setId(10L);
        mockCourse.setCourseCode("CAIS 0236");
        mockCourse.setCourseName("Computer Organization and Architecture");
        mockCourse.setDepartmentCode("CAIS");

        mockEnrollment = new UserCourse();
        mockEnrollment.setId(100L);
        mockEnrollment.setSection("001");
        mockEnrollment.setSemester("Fall 2026");

        enrollRequest = new CourseEnrollRequest();
        enrollRequest.setCourseCode("CAIS 0236");
        enrollRequest.setSection("001");
        enrollRequest.setSemester("Fall 2026");
    }

    // stubs authentication.getName() — only called in tests that pass authentication to the controller
    private void mockAuth() {
        when(authentication.getName()).thenReturn(EMAIL);
    }

    // ── GET /courses ───────────────────────────────────────────────────────────

    @Test
    void getAllCourses_returns200WithCourseList() {
        when(courseService.getAllCourses()).thenReturn(List.of(mockCourse));
        ResponseEntity<List<Course>> response = courseController.getAllCourses();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getAllCourses_emptyCatalog_returns200WithEmptyList() {
        when(courseService.getAllCourses()).thenReturn(List.of());
        ResponseEntity<List<Course>> response = courseController.getAllCourses();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    // ── GET /courses/my ────────────────────────────────────────────────────────

    @Test
    void getMyCourses_returns200WithEnrollmentList() {
        mockAuth();
        when(courseService.getMyCourses(EMAIL)).thenReturn(List.of(mockEnrollment));
        ResponseEntity<List<UserCourse>> response = courseController.getMyCourses(authentication);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getMyCourses_passesEmailFromPrincipalToService() {
        mockAuth();
        when(courseService.getMyCourses(EMAIL)).thenReturn(List.of());
        courseController.getMyCourses(authentication);
        verify(courseService, times(1)).getMyCourses(EMAIL);
    }

    @Test
    void getMyCourses_profileNotFound_propagatesResourceNotFoundException() {
        mockAuth();
        when(courseService.getMyCourses(EMAIL))
                .thenThrow(new ResourceNotFoundException("Profile not found"));
        assertThrows(ResourceNotFoundException.class,
                () -> courseController.getMyCourses(authentication));
    }

    // ── POST /courses/enroll ───────────────────────────────────────────────────

    @Test
    void enroll_returns201WithCreatedEnrollment() {
        mockAuth();
        when(courseService.enroll(eq(EMAIL), any(CourseEnrollRequest.class))).thenReturn(mockEnrollment);
        ResponseEntity<UserCourse> response = courseController.enroll(authentication, enrollRequest);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(mockEnrollment, response.getBody());
    }

    @Test
    void enroll_doesNotReturn200_mustBe201() {
        mockAuth();
        when(courseService.enroll(eq(EMAIL), any(CourseEnrollRequest.class))).thenReturn(mockEnrollment);
        ResponseEntity<UserCourse> response = courseController.enroll(authentication, enrollRequest);
        assertNotEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void enroll_courseNotFound_propagatesResourceNotFoundException() {
        mockAuth();
        when(courseService.enroll(eq(EMAIL), any(CourseEnrollRequest.class)))
                .thenThrow(new ResourceNotFoundException("Course not found"));
        assertThrows(ResourceNotFoundException.class,
                () -> courseController.enroll(authentication, enrollRequest));
    }

    @Test
    void enroll_duplicateEnrollment_propagates409ConflictException() {
        mockAuth();
        when(courseService.enroll(eq(EMAIL), any(CourseEnrollRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Already enrolled"));
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> courseController.enroll(authentication, enrollRequest));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void enroll_passesEmailAndRequestToService() {
        mockAuth();
        when(courseService.enroll(eq(EMAIL), eq(enrollRequest))).thenReturn(mockEnrollment);
        courseController.enroll(authentication, enrollRequest);
        verify(courseService, times(1)).enroll(EMAIL, enrollRequest);
    }

    // ── DELETE /courses/{userCourseId} ─────────────────────────────────────────

    @Test
    void drop_ownEnrollment_returns204NoContent() {
        mockAuth();
        doNothing().when(courseService).drop(100L, EMAIL);
        ResponseEntity<Void> response = courseController.drop(authentication, 100L);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void drop_passesEnrollmentIdAndEmailToService() {
        mockAuth();
        doNothing().when(courseService).drop(100L, EMAIL);
        courseController.drop(authentication, 100L);
        verify(courseService, times(1)).drop(100L, EMAIL);
    }

    @Test
    void drop_enrollmentNotFound_propagatesResourceNotFoundException() {
        mockAuth();
        doThrow(new ResourceNotFoundException("Enrollment not found"))
                .when(courseService).drop(100L, EMAIL);
        assertThrows(ResourceNotFoundException.class,
                () -> courseController.drop(authentication, 100L));
    }

    @Test
    void drop_enrollmentOwnedByAnotherStudent_propagates403ForbiddenException() {
        mockAuth();
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your enrollment"))
                .when(courseService).drop(100L, EMAIL);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> courseController.drop(authentication, 100L));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    // ── GET /courses/search ────────────────────────────────────────────────────

    @Test
    void searchCourses_returns200WithResults() {
        when(courseService.searchCourses("biology")).thenReturn(List.of(mockCourse));
        ResponseEntity<List<Course>> response = courseController.searchCourses("biology");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void searchCourses_noMatches_returns200WithEmptyList() {
        when(courseService.searchCourses("zzzz")).thenReturn(List.of());
        ResponseEntity<List<Course>> response = courseController.searchCourses("zzzz");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void searchCourses_passesKeywordToService() {
        when(courseService.searchCourses("biology")).thenReturn(List.of());
        courseController.searchCourses("biology");
        verify(courseService, times(1)).searchCourses("biology");
    }

    // ── GET /courses/department/{code} ─────────────────────────────────────────

    @Test
    void getCoursesByDepartment_returns200WithFilteredList() {
        when(courseService.getCoursesByDepartment("CAIS")).thenReturn(List.of(mockCourse));
        ResponseEntity<List<Course>> response = courseController.getCoursesByDepartment("CAIS");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getCoursesByDepartment_unknownDepartment_returns200WithEmptyList() {
        when(courseService.getCoursesByDepartment("FAKE")).thenReturn(List.of());
        ResponseEntity<List<Course>> response = courseController.getCoursesByDepartment("FAKE");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void getCoursesByDepartment_passesDepartmentCodeToService() {
        when(courseService.getCoursesByDepartment("MATH")).thenReturn(List.of());
        courseController.getCoursesByDepartment("MATH");
        verify(courseService, times(1)).getCoursesByDepartment("MATH");
    }
}
