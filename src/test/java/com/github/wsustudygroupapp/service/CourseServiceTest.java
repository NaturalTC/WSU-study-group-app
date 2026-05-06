package com.github.wsustudygroupapp.service;

import com.github.wsustudygroupapp.model.Course;
import com.github.wsustudygroupapp.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock private CourseRepository courseRepository;
    @InjectMocks private CourseService courseService;

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
    void getAllCourses_returnsFullCatalogFromRepo() {
        when(courseRepository.findAll()).thenReturn(List.of(mockCourse));
        assertEquals(List.of(mockCourse), courseService.getAllCourses());
    }

    @Test
    void getAllCourses_emptyCatalog_returnsEmptyList() {
        when(courseRepository.findAll()).thenReturn(List.of());
        assertTrue(courseService.getAllCourses().isEmpty());
    }
}
