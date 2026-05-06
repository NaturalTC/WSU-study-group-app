package com.github.wsustudygroupapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Request body for POST /profile (create) and PUT /profile (update).
 * Sent by the frontend when a student sets up or edits their profile.
 */
@Data
@Schema(description = "Profile creation or update request")
public class ProfileRequest {

    @Schema(description = "Student's display name", example = "Jose Jimenez")
    private String name;

    @Schema(description = "Student's declared major", example = "Computer Science")
    private String major;

    @Schema(description = "Academic year", example = "Junior", allowableValues = {"Freshman", "Sophomore", "Junior", "Senior"})
    private String year;

    @Schema(description = "Optional short bio visible to other group members", example = "CS junior, love algorithms and coffee")
    private String bio;
}
