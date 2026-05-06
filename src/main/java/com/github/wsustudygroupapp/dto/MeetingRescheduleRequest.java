package com.github.wsustudygroupapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Request body for PATCH /meetings/{id}.
 * Used by a session creator to reschedule or edit details. All fields are optional —
 * a null field on a PATCH means "leave the existing value alone."
 */
@Data
@Schema(description = "Meeting session reschedule/edit request")
public class MeetingRescheduleRequest {

    @Schema(description = "New date and time for the session (null = leave unchanged, edit details only)",
            example = "2026-04-22T15:30:00")
    @Future(message = "scheduledAt must be in the future")
    private LocalDateTime scheduledAt;

    @Schema(description = "Updated duration in minutes (null = leave unchanged, range 1–480)",
            example = "90")
    @Min(value = 1,   message = "durationMinutes must be at least 1")
    @Max(value = 480, message = "durationMinutes must be 480 or less (8 hours)")
    private Integer durationMinutes;

    @Schema(description = "Updated location (optional)", example = "Bates Hall Rm 110")
    @Size(max = 200, message = "location must be 200 characters or fewer")
    private String location;

    @Schema(description = "Updated notes/agenda (optional)", example = "Moved to a quieter room")
    @Size(max = 2000, message = "notes must be 2000 characters or fewer")
    private String notes;
}
