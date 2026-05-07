package com.github.wsustudygroupapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for real-time chat messages sent over WebSocket.
 * Used in both directions — the frontend sends it to /app/chat/{groupId},
 * and the server broadcasts it back to all members subscribed to /topic/chat/{groupId}.
 */
@Data
@Schema(description = "WebSocket chat message — sent and received by all members of a study group")
public class MessageDTO {

    @Schema(description = "ID of the study group this message belongs to", example = "7")
    private Long studyGroupId;

    @Schema(description = "Display name of the student who sent the message", example = "Jose Jimenez")
    private String senderName;

    @Schema(description = "The message text", example = "Does anyone understand question 3?")
    private String content;

    @Schema(description = "Timestamp set by the server when the message is received", example = "2026-04-01T19:30:00")
    private LocalDateTime sentAt;

    @Schema(description = "DM room ID (e.g. 'dm-1-5'). Null for group messages.", example = "dm-1-5")
    private String dmRoomId;
}
