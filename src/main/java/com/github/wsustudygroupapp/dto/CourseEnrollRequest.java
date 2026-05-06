package com.github.wsustudygroupapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Request body for POST /courses/enroll.
 * Sent by the frontend when a student adds a course to their schedule.
 */
@Data
@Schema(description = "Course enrollment request")
public class CourseEnrollRequest {

    @Schema(description = "Course code from the WSU catalog", example = "CAIS 0236")
    private String courseCode;

}
