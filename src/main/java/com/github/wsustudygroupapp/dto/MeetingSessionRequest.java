package com.github.wsustudygroupapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
    private Long groupId;

    @Schema(description = "Date and time the session starts", example = "2026-04-15T14:00:00")
    private LocalDateTime scheduledAt;

    @Schema(description = "Where the session will take place", example = "Ely Library Rm 204")
    private String location;

    @Schema(description = "Optional agenda or notes for the session", example = "Reviewing chapters 5 and 6 for the midterm")
    private String notes;
}
