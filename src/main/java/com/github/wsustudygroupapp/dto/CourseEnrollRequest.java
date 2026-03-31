package com.github.wsustudygroupapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Request body for POST /courses/enroll.
 * Sent by the frontend when a student adds a course to their schedule.
 * The section and semester are required for classmate matching to work correctly.
 */
@Data
@Schema(description = "Course enrollment request — section and semester are required for classmate matching")
public class CourseEnrollRequest {

    @Schema(description = "Course code from the WSU catalog", example = "CAIS 0236")
    private String courseCode;

    @Schema(description = "The student's specific class section", example = "001")
    private String section;

    @Schema(description = "The semester of enrollment", example = "Fall 2026")
    private String semester;
}
