package com.github.wsustudygroupapp.controller;

import com.github.wsustudygroupapp.model.Course;
import com.github.wsustudygroupapp.service.CourseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseControllerTest {

    @Mock private CourseService courseService;
    @InjectMocks private CourseController courseController;

    private Course mockCourse;

    @BeforeEach
    void setUp() {
        mockCourse = new Course();
        mockCourse.setId(10L);
        mockCourse.setCourseCode("CAIS 0236");
        mockCourse.setCourseName("Computer Organization and Architecture");
        mockCourse.setDepartmentCode("CAIS");
    }

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
}
