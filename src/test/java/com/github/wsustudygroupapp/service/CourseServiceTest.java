package com.github.wsustudygroupapp.service;

import com.github.wsustudygroupapp.dto.CourseEnrollRequest;
import com.github.wsustudygroupapp.exception.ResourceNotFoundException;
import com.github.wsustudygroupapp.model.Course;
import com.github.wsustudygroupapp.model.Profile;
import com.github.wsustudygroupapp.model.User;
import com.github.wsustudygroupapp.model.UserCourse;
import com.github.wsustudygroupapp.repository.CourseRepository;
import com.github.wsustudygroupapp.repository.ProfileRepository;
import com.github.wsustudygroupapp.repository.UserCourseRepository;
import com.github.wsustudygroupapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock private CourseRepository courseRepository;
    @Mock private UserCourseRepository userCourseRepository;
    @Mock private ProfileRepository profileRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private CourseService courseService;

    private static final String EMAIL = "test@westfield.ma.edu";
    private static final String UNKNOWN_EMAIL = "ghost@westfield.ma.edu";

    private User mockUser;
    private Profile mockProfile;
    private Profile otherProfile;
    private Course mockCourse;
    private UserCourse mockEnrollment;
    private CourseEnrollRequest enrollRequest;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail(EMAIL);

        mockProfile = new Profile();
        mockProfile.setId(1L);
        mockProfile.setUser(mockUser);

        otherProfile = new Profile();
        otherProfile.setId(99L);

        mockCourse = new Course();
        mockCourse.setId(10L);
        mockCourse.setCourseCode("CAIS 0236");
        mockCourse.setCourseName("Computer Organization and Architecture");
        mockCourse.setDepartmentCode("CAIS");

        mockEnrollment = new UserCourse();
        mockEnrollment.setId(100L);
        mockEnrollment.setProfile(mockProfile);
        mockEnrollment.setCourse(mockCourse);
        mockEnrollment.setSection("001");
        mockEnrollment.setSemester("Fall 2026");

        enrollRequest = new CourseEnrollRequest();
        enrollRequest.setCourseCode("CAIS 0236");
        enrollRequest.setSection("001");
        enrollRequest.setSemester("Fall 2026");
    }

    // stubs the resolveProfile() helper used by most service methods
    private void stubResolveProfile() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(mockProfile));
    }

    // ── getAllCourses ──────────────────────────────────────────────────────────

    @Test
    void getAllCourses_returnsFullCatalogFromRepo() {
        when(courseRepository.findAll()).thenReturn(List.of(mockCourse));
        assertEquals(List.of(mockCourse), courseService.getAllCourses());
    }

    @Test
    void getAllCourses_emptyCatalog_returnsEmptyList() {
        when(courseRepository.findAll()).thenReturn(List.of());
        assertTrue(courseService.getAllCourses().isEmpty());
    }

    // ── getMyCourses ───────────────────────────────────────────────────────────

    @Test
    void getMyCourses_userNotFound_throwsResourceNotFoundException() {
        when(userRepository.findByEmail(UNKNOWN_EMAIL)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> courseService.getMyCourses(UNKNOWN_EMAIL));
    }

    @Test
    void getMyCourses_profileNotFound_throwsResourceNotFoundException() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> courseService.getMyCourses(EMAIL));
    }

    @Test
    void getMyCourses_returnsEnrollmentsForCorrectProfile() {
        stubResolveProfile();
        when(userCourseRepository.findByProfileId(1L)).thenReturn(List.of(mockEnrollment));
        assertEquals(List.of(mockEnrollment), courseService.getMyCourses(EMAIL));
    }

    @Test
    void getMyCourses_studentNotEnrolledInAnything_returnsEmptyList() {
        stubResolveProfile();
        when(userCourseRepository.findByProfileId(1L)).thenReturn(List.of());
        assertTrue(courseService.getMyCourses(EMAIL).isEmpty());
    }

    // ── enroll ─────────────────────────────────────────────────────────────────

    @Test
    void enroll_userNotFound_throwsResourceNotFoundException() {
        when(userRepository.findByEmail(UNKNOWN_EMAIL)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> courseService.enroll(UNKNOWN_EMAIL, enrollRequest));
    }

    @Test
    void enroll_courseCodeNotInCatalog_throwsResourceNotFoundException() {
        stubResolveProfile();
        when(courseRepository.findByCourseCode("CAIS 0236")).thenReturn(Optional.empty());
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> courseService.enroll(EMAIL, enrollRequest));
        assertTrue(ex.getMessage().contains("CAIS 0236"));
    }

    @Test
    void enroll_alreadyEnrolledInSameSectionAndSemester_throws409Conflict() {
        stubResolveProfile();
        when(courseRepository.findByCourseCode("CAIS 0236")).thenReturn(Optional.of(mockCourse));
        when(userCourseRepository.existsByProfileIdAndCourseIdAndSectionAndSemester(
                1L, 10L, "001", "Fall 2026")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> courseService.enroll(EMAIL, enrollRequest));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void enroll_differentSectionSameCourse_doesNotThrowConflict() {
        // enrolling in a different section of the same course should be allowed
        stubResolveProfile();
        CourseEnrollRequest differentSection = new CourseEnrollRequest();
        differentSection.setCourseCode("CAIS 0236");
        differentSection.setSection("002");
        differentSection.setSemester("Fall 2026");

        when(courseRepository.findByCourseCode("CAIS 0236")).thenReturn(Optional.of(mockCourse));
        when(userCourseRepository.existsByProfileIdAndCourseIdAndSectionAndSemester(
                1L, 10L, "002", "Fall 2026")).thenReturn(false);
        when(userCourseRepository.save(any(UserCourse.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> courseService.enroll(EMAIL, differentSection));
    }

    @Test
    void enroll_setsAllFieldsOnNewEnrollment() {
        stubResolveProfile();
        when(courseRepository.findByCourseCode("CAIS 0236")).thenReturn(Optional.of(mockCourse));
        when(userCourseRepository.existsByProfileIdAndCourseIdAndSectionAndSemester(
                1L, 10L, "001", "Fall 2026")).thenReturn(false);
        when(userCourseRepository.save(any(UserCourse.class))).thenAnswer(inv -> inv.getArgument(0));

        UserCourse result = courseService.enroll(EMAIL, enrollRequest);

        assertEquals(mockProfile, result.getProfile());
        assertEquals(mockCourse, result.getCourse());
        assertEquals("001", result.getSection());
        assertEquals("Fall 2026", result.getSemester());
    }

    @Test
    void enroll_persistsEnrollmentExactlyOnce() {
        stubResolveProfile();
        when(courseRepository.findByCourseCode("CAIS 0236")).thenReturn(Optional.of(mockCourse));
        when(userCourseRepository.existsByProfileIdAndCourseIdAndSectionAndSemester(
                1L, 10L, "001", "Fall 2026")).thenReturn(false);
        when(userCourseRepository.save(any(UserCourse.class))).thenReturn(mockEnrollment);

        courseService.enroll(EMAIL, enrollRequest);

        verify(userCourseRepository, times(1)).save(any(UserCourse.class));
    }

    // ── drop ───────────────────────────────────────────────────────────────────

    @Test
    void drop_userNotFound_throwsResourceNotFoundException() {
        when(userRepository.findByEmail(UNKNOWN_EMAIL)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> courseService.drop(100L, UNKNOWN_EMAIL));
    }

    @Test
    void drop_enrollmentNotFound_throwsResourceNotFoundException() {
        stubResolveProfile();
        when(userCourseRepository.findById(100L)).thenReturn(Optional.empty());
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> courseService.drop(100L, EMAIL));
        assertTrue(ex.getMessage().contains("100"));
    }

    @Test
    void drop_enrollmentOwnedByAnotherStudent_throws403Forbidden() {
        stubResolveProfile();
        // enrollment belongs to a different profile
        mockEnrollment.setProfile(otherProfile);
        when(userCourseRepository.findById(100L)).thenReturn(Optional.of(mockEnrollment));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> courseService.drop(100L, EMAIL));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void drop_enrollmentOwnedByAnotherStudent_doesNotDelete() {
        // must not delete anything if the ownership check fails
        stubResolveProfile();
        mockEnrollment.setProfile(otherProfile);
        when(userCourseRepository.findById(100L)).thenReturn(Optional.of(mockEnrollment));

        assertThrows(ResponseStatusException.class, () -> courseService.drop(100L, EMAIL));
        verify(userCourseRepository, never()).delete(any());
    }

    @Test
    void drop_ownEnrollment_deletesCorrectRecord() {
        stubResolveProfile();
        when(userCourseRepository.findById(100L)).thenReturn(Optional.of(mockEnrollment));

        courseService.drop(100L, EMAIL);

        verify(userCourseRepository, times(1)).delete(mockEnrollment);
    }

    // ── getClassmates ──────────────────────────────────────────────────────────

    @Test
    void getClassmates_enrollmentNotFound_throwsResourceNotFoundException() {
        stubResolveProfile();
        when(userCourseRepository.findById(100L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> courseService.getClassmates(100L, EMAIL));
    }

    @Test
    void getClassmates_queriesRepoWithCorrectCourseAndSectionAndSemester() {
        stubResolveProfile();
        when(userCourseRepository.findById(100L)).thenReturn(Optional.of(mockEnrollment));
        when(userCourseRepository.findClassmates(10L, "001", "Fall 2026", 1L))
                .thenReturn(List.of());

        courseService.getClassmates(100L, EMAIL);

        // verify the correct values are passed to the matching query
        verify(userCourseRepository).findClassmates(10L, "001", "Fall 2026", 1L);
    }

    @Test
    void getClassmates_returnsClassmatesFromRepo() {
        stubResolveProfile();
        UserCourse classmate = new UserCourse();
        when(userCourseRepository.findById(100L)).thenReturn(Optional.of(mockEnrollment));
        when(userCourseRepository.findClassmates(10L, "001", "Fall 2026", 1L))
                .thenReturn(List.of(classmate));

        assertEquals(1, courseService.getClassmates(100L, EMAIL).size());
    }

    @Test
    void getClassmates_noMatchingStudents_returnsEmptyList() {
        stubResolveProfile();
        when(userCourseRepository.findById(100L)).thenReturn(Optional.of(mockEnrollment));
        when(userCourseRepository.findClassmates(10L, "001", "Fall 2026", 1L))
                .thenReturn(List.of());

        assertTrue(courseService.getClassmates(100L, EMAIL).isEmpty());
    }

    // ── searchCourses ──────────────────────────────────────────────────────────

    @Test
    void searchCourses_returnsMatchingCourses() {
        when(courseRepository.findByCourseNameContainingIgnoreCase("biology"))
                .thenReturn(List.of(mockCourse));
        assertEquals(List.of(mockCourse), courseService.searchCourses("biology"));
    }

    @Test
    void searchCourses_keywordWithNoMatches_returnsEmptyList() {
        when(courseRepository.findByCourseNameContainingIgnoreCase("zzzzzz"))
                .thenReturn(List.of());
        assertTrue(courseService.searchCourses("zzzzzz").isEmpty());
    }

    @Test
    void searchCourses_isCaseInsensitive() {
        // repo method handles case-insensitivity — verify it's called with the raw keyword
        courseService.searchCourses("BIOLOGY");
        verify(courseRepository).findByCourseNameContainingIgnoreCase("BIOLOGY");
    }

    // ── getCoursesByDepartment ─────────────────────────────────────────────────

    @Test
    void getCoursesByDepartment_returnsFilteredCourses() {
        when(courseRepository.findByDepartmentCode("CAIS")).thenReturn(List.of(mockCourse));
        assertEquals(List.of(mockCourse), courseService.getCoursesByDepartment("CAIS"));
    }

    @Test
    void getCoursesByDepartment_unknownDepartment_returnsEmptyList() {
        when(courseRepository.findByDepartmentCode("FAKE")).thenReturn(List.of());
        assertTrue(courseService.getCoursesByDepartment("FAKE").isEmpty());
    }
}
