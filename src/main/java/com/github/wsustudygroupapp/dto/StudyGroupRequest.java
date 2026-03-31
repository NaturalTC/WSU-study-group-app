package com.github.wsustudygroupapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Request body for POST /groups.
 * Sent by the frontend when a student creates a new study group.
 * The creating student is automatically added as the first member.
 */
@Data
@Schema(description = "Study group creation request")
public class StudyGroupRequest {

    @Schema(description = "Display name for the study group", example = "CAIS 0236 Study Crew")
    private String name;

    @Schema(description = "ID of the course this group is for", example = "42")
    private Long courseId;
}
