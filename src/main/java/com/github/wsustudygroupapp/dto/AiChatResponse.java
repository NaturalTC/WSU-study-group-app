package com.github.wsustudygroupapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response body for POST /ai/chat.
 * Returned to the frontend after the backend receives a reply from the ChatGPT API.
 * The frontend renders this reply as an AI message visible to all group members.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI study assistant chat response")
public class AiChatResponse {

    @Schema(description = "The AI-generated reply to the student's message")
    private String reply;

    // TODO: Sprint 2 — add a sessionId field so the frontend can send it back
    //       in the next request to maintain multi-turn conversation context
}
