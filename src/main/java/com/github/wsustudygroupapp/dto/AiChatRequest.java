package com.github.wsustudygroupapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Request body for POST /ai/chat.
 * Sent by the frontend when a student sends a message to the AI assistant in a group chat.
 * The backend proxies this to the ChatGPT API and returns the reply.
 */
@Data
@Schema(description = "AI study assistant chat request")
public class AiChatRequest {

    @Schema(description = "ID of the study group the student is chatting in", example = "3")
    private Long groupId;

    @Schema(description = "ID of the profile sending the message", example = "7")
    private Long profileId;

    @Schema(description = "The student's message or question to the AI", example = "Can you explain binary search trees?")
    private String message;

    // TODO: Sprint 2 — add a sessionId field to maintain conversation context across messages
    //       so the AI can refer back to earlier questions in the same session
}
