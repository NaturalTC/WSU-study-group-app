package com.github.wsustudygroupapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Request body for POST /meetings.
 * Sent by the frontend when a group member schedules a new study session.
 * All other group members will receive a notification after the session is created.
 */
@Data
@Schema(description = "Meeting session creation request")
public class MeetingSessionRequest {

    @Schema(description = "ID of the study group this session belongs to", example = "2")
    @NotNull(message = "groupId is required")
    private Long groupId;

    @Schema(description = "Date and time the session starts (must be in the future)",
            example = "2026-04-15T14:00:00")
    @NotNull(message = "scheduledAt is required")
    @Future(message = "scheduledAt must be in the future")
    private LocalDateTime scheduledAt;

    @Schema(description = "Expected length of the session in minutes (optional, 1–480)",
            example = "60")
    @Min(value = 1,   message = "durationMinutes must be at least 1")
    @Max(value = 480, message = "durationMinutes must be 480 or less (8 hours)")
    private Integer durationMinutes;

    @Schema(description = "Where the session will take place", example = "Ely Library Rm 204")
    @Size(max = 200, message = "location must be 200 characters or fewer")
    private String location;

    @Schema(description = "Optional agenda or notes for the session",
            example = "Reviewing chapters 5 and 6 for the midterm")
    @Size(max = 2000, message = "notes must be 2000 characters or fewer")
    private String notes;
}
